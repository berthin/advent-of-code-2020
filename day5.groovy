data = new File("day5.in") as String[]

decode = { x, on , off -> x.replace(on, '1').replace(off, '0') }
row = { x -> Integer.parseInt(decode(x, 'B', 'F'), 2) }
col = { x -> Integer.parseInt(decode(x, 'R', 'L'), 2) }
seat = { x, k -> row(x[0..k-1]) * 2 ** (x.size() - k) + col(x[k..-1]) }

seats = data.collect { instructions -> seat(instructions, 7) }

println "HighestID: ${seats.max()}"

mySeat = (0..seats.size()).collect { id -> seats.contains(id) ? -1 : id }.max()
println "My seat ID: $mySeat"
