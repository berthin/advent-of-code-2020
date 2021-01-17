from pathlib import Path
from functools import *
from itertools import *

def separate(lines, sep):
    group = []
    for line in lines + [sep]:
        if str(line) == str(sep) and len(group) > 0:
            yield group
            group = []
        else:
            group.append(line)

data = Path('day6.in').read_text().splitlines()
groups = list(separate(data, sep=''))
intersection = lambda x, y: x & y
union = lambda x, y: x | y
solve = lambda func, group: len(reduce(func, map(lambda g: set(list(g)), group)))

partA = partial(solve, union)
partB = partial(solve, intersection)

print(sum(map(partA, groups)))
print(sum(map(partB, groups)))
