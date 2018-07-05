result = 0

def adder(num):
    global result
    result += num
    return result

print(adder(50))
print(adder(40))

