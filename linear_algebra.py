import numpy as np
import matplotlib.pyplot as plt

a = np.matrix('2 3; 4 5')
b = np.matrix('1 2 3; 4 5 6;7 8 9')
c = np.tensordot(a,b,axes=0)
print c

x = np.random.rand(3, 2) * 10
a = np.matrix([ [1,x[0][0]], [1,x[1][0]], [1,x[2][0]] ])
b = np.matrix([ [x[0][1]], [x[1][1]], [x[2][1]] ])
yy = (a.T * a).I * a.T * b
xx = np.linspace(1, 10, 50)
y = np.array(yy[0] + yy[1] * xx)

#plt.figure(1)
#plt.plot(xx, y.T, color='r')
#plt.scatter([x[0][0], x[1][0], x[2][0] ], [x[0][1], x[1][1], x[2][1] ]) 
#plt.show()

x = np.array([4,5,7,23])
y = np.array([1,2.3,4.5,6])

#y = mX +x => y = Ap => A = [[x 1]], p = [[m], [c]]

A = np.vstack([x, np.ones(len(x))]).T
m,c = np.linalg.lstsq(A, y)[0]
print m,c

plt.plot(x, y, 'o', label='Original data', markersize=10)
plt.plot(x, m*x + c, 'r', label='Fitted line')
plt.legend()
plt.show() 