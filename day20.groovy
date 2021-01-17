import java.lang.String
import groovy.transform.Field

import static Constants.*


class Constants {
  static final EDGE = 10
}


class Border {
  String border
  int id

  Border(String border, int id=-1) {
    this.border = border
    this.id = id
  }

  String getSide(int side) {// side : top, right, bottom, left
    return this.border[(side) * EDGE .. (side + 1) * EDGE - 1]
  }

  Border rotateBy(int rot) {
    String borderExt = this.border * 2
    return new Border(borderExt[((4 - rot) * EDGE)..((8 - rot) * EDGE) - 1], this.id)
  }

  Border flip(int perform) { // 0: false, 1: true
    if (perform == 0) {
      return new Border(border, id)
    }

    return new Border(
      [0, 3, 2, 1].collect { side -> getSide(side).reverse() }.join(),
      this.id
    )
  }

  boolean isConnectedTo(Border other, int side) {
    // println "${this.id} ${this.getSide(side)} == ${other.id} ${other.getSide((side + 2) % 4).reverse()}"
    return this.getSide(side) == other.getSide((side + 2) % 4).reverse()
  }

  void print() {
    println this.border
  }
}


class Tile {
  int id
  int dim
  List content // content = dim x dim

  Tile (List text, int id) {
    this.id = id
    this.content = text.collect()
    this.dim = content.size()
  }

  Tile rotate() {
    List rotated = content.collect{ it.toCharArray() }
    int N = this.dim - 1

    [(0..N), (0..N)].combinations().each { row, col ->
      rotated[col][N - row] = content[row][col]
    }
    this.content = rotated.collect { it as String }

    this
  }

  Tile rotateBy(int rot) {
    if (rot > 0) {
      (1..rot).each { rotate() }
    }

    this
  }

  Tile flip(int perform) {
    if (perform == 1) {
      this.content = this.content.collect { row -> row.reverse() }
    }

    this
  }

  Border getBorder() {
    String border = this.content[0] + // top (from right_left)
        this.content.collect { it[-1] }.join() + // right (from up_bottom)
        this.content[-1].reverse() + // bottom (from right_left)
        this.content.collect { it[0] }.join().reverse() // left (from bottom_top)

    return new Border(border, this.id)
  }

  void print() {
    content.each { println it }
  }
}


class Image {
  List tiles
  int dim

  Image(List tiles) {
    this.tiles = tiles
    this.dim = this.tiles.size().power(0.5) as int
  }

  List filterNeighbors(List neighbors, int connectivity) {
    return neighbors.withIndex().findAll { neighbor, idx -> neighbor == connectivity }*.last()
  }

  List reconstruct() {
    List neighbors, borders
    def metadata = [:]

    (borders, neighbors, metadata) = getNeighbors()

    List corners = filterNeighbors(neighbors, 2)
    List edges = filterNeighbors(neighbors, 3)
    List middle = filterNeighbors(neighbors, 4)

    print "tiles  : "
    tiles.eachWithIndex { it, idx -> print "(${it.id} $idx) " }
    println ""

    println "corners: ${corners}"
    println "edges  : ${edges}"
    println "middle : ${middle}"

    long partI = corners.inject(1) { ans, cur -> ans * (tiles[cur].id as long) }
    println "PartI ${partI}"

    def nodes = bfs(0, metadata, borders)
    int top_left_row = nodes*.row.min()
    int top_left_col = nodes*.col.min()

    def img = new int[dim][dim]
    nodes.each { node ->
      img[node.row - top_left_row][node.col - top_left_col] = node.index
      tiles[node.index].flip(node.flip).rotateBy(node.rot)
    }

    img.each { println it.collect { x -> tiles[x].id } }

    int reducedEdge = EDGE - 2
    List result = [""] * (this.dim  * reducedEdge)
    // [(0..this.dim-1), (0..this.dim-1)].combinations().each { col, row ->
    for (int row in 0..this.dim-1) {
      for (int col in 0..this.dim-1) {
        Tile tile = tiles[img[row][col]]
        (1..reducedEdge).each { depth ->
          result[row * reducedEdge + depth - 1] += tile.content[depth][1..reducedEdge]
        }
      }
    }

    return result
  }

  def bfs(int root, def graph, List borders) {
    List nodes = []

    List visited = new Boolean[graph.size()]
    Queue queue = [] as Queue

    List mv = [[-1, 0], [0, +1], [+1, 0], [0, -1]]
    queue << [index: root, row: 0, col: 0, rot: 0, flip: 0]

    while (queue.size() > 0) {
      def node = queue.poll()
      if (visited[node.index]) continue

      visited[node.index] = true

      nodes << node
      println "Adding node: $node"

      Border border = borders[node.index].flip(node.flip).rotateBy(node.rot)

      graph[node.index].each { _, neighbor ->
        if (visited[neighbor.index]) return

        [(0..1), (0..3)].combinations().each { flipN, side ->
          Border borderN = borders[neighbor.index].flip(flipN)

          (0..3).each { sideN ->
            if (border.isConnectedTo(borderN.rotateBy(sideN), side)) {
              queue << ["index": neighbor.index,
                        parent: node.index,
                        row: node.row + mv[side][0],
                        col: node.col + mv[side][1],
                        parent_edge: side,
                        rot: sideN,
                        flip: flipN]
            }
          }
        }
      }
    }

    return nodes
  }

  List getNeighbors() {
    List borders = tiles.collect { it.getBorder() }

    List neighbors = [0] * borders.size()
    def metadata = [[:]] * borders.size()

    borders.eachWithIndex() { borderA, indexA ->
      borders.eachWithIndex() { borderB, indexB ->
        if (indexA == indexB) return

        [(0..1), (0..3), (0..3)].combinations().each { flipB, sideA, sideB ->
          Border borderAA = borderA.rotateBy(sideA)
          Border borderBB = borderB.flip(flipB).rotateBy(sideB)
          if (borderAA.isConnectedTo(borderBB, 1)) {
            metadata[indexA] += ["$indexB": [index: indexB, sideA: sideA, sideB: sideB, flippedB: flipB]]
            neighbors[indexA] += 1
          }
        }

      }
    }

    metadata.eachWithIndex { it, idx -> println "$idx, $it" }
    println "neighbors: $neighbors"

    return [borders, neighbors, metadata]
  }
}

static List readTilesFromInput(List data) {
  List tileNameIndex = (0..data.size() - 1).findAll { idx -> data[idx].contains(":") }

  List tiles = tileNameIndex.collect { idx -> new Tile(data[idx+1..idx+EDGE], -1) }
  println "#tiles: ${tiles.size()}"
  tileNameIndex.eachWithIndex() { idxData, idxTile ->
    tiles[idxTile].id = data[idxData].findAll(/\d+/)[0] as int
  }

  return tiles
}

static int compare(List imgA, List imgB) {
  String a = imgA.join()
  String b = imgB.join()

  boolean match = true
  int answer = 0

  b.eachWithIndex { bb, idx ->
    if (bb == "#")
      match &= (a[idx] == "#")
    else
      answer += (a[idx] == "#" ? 1 : 0)
  }

  return match ? 1: 0
}

static int searchSeaMonster(List img) {
  List seaMonster = [
    "                  # ",
    "#    ##    ##    ###",
    " #  #  #  #  #  #   "]


  int seaMonsterCols = seaMonster[0].size()
  int seaMonsterRows = seaMonster.size()

  int imgCols = img[0].size()
  int imgRows = img.size()

  int counter = 0

  (0..imgRows - seaMonsterRows).each { row ->
    (0..imgCols - seaMonsterCols).each { col ->
      def patch = img[row..row+seaMonsterRows-1].collect { it[col..col+seaMonsterCols-1] }
      counter += compare(patch, seaMonster)
    }
  }

  int hashImg = img.join().collect { it.collect { k -> k == "#" ? 1 : 0 }.sum() }.sum()
  int hashSeaMonster = seaMonster.join().collect { it.collect { k -> k == "#" ? 1 : 0 }.sum() }.sum()

  println "$hashImg $hashSeaMonster $counter"
  return hashImg - hashSeaMonster * counter
}

static void solve(String fileName) {
  String[] dataAsString = new File(fileName) as String[]
  List data = dataAsString.collect { it }

  List tiles = readTilesFromInput(data)
  Image img = new Image(tiles)

  Tile reconstructedImg = new Tile(img.reconstruct(), -1)
  reconstructedImg.content.each { println it }

  int answer = 1<<30
  [0,1].each { flip ->
    reconstructedImg.flip(flip)

    (0..3).each { rot ->
      reconstructedImg.rotate()
      answer = Math.min(answer, searchSeaMonster(reconstructedImg.content))
    }
  }

  println "PartII ${answer}"
}


solve("day20.in")
