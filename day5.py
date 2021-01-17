from pathlib import Path

data = Path('day5.in').read_text().splitlines()

decode = lambda x, on, off: x.replace(on, '1').replace(off, '0')
row = lambda x: int(decode(x, 'B', 'F'), 2)
col = lambda x: int(decode(x, 'R', 'L'), 2)
seat = lambda x, k: row(x[:k]) * 2 ** (len(x) - k) + col(x[k:])

seats = [seat(instructions, 7) for instructions in data]
print('HighestID', max(seats), min(seats), len(seats))
print('My seat ID', max(seat for seat in range(len(seats)) if seat not in seats))

