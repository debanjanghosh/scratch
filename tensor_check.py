import theano
import theano.tensor as T
import numpy as np
a = np.matrix('1 2; 3 4')
b = np.matrix('5 6; 8 9')

aa = theano.shared(a)
bb = theano.shared(b)
print aa
print bb

c = theano.tensor.tensordot(aa, bb)
c = T.prod(aa,bb)
print c