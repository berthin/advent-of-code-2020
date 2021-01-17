data = new File('day6.in') as String[]

// Split the data into groups, each group will have a collection of Sets of answers
groups = [[]]
data.each { line ->
    if (line.isEmpty()) groups << []
    else groups[-1].add(line.split('') as Set)
}

solve = { func, group ->
    group.inject() { ans, answer -> func(ans, answer) }.size()
}

UnionFn = { x, y -> x + y }
InteresectionFn = { x, y -> x.intersect(y) }

print "PartI "
println groups.collect { group -> solve(UnionFn, group) }.sum()

print "PartII "
println groups.collect { group -> solve(InteresectionFn, group) }.sum()
