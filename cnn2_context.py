"""
Sample code for
Convolutional Neural Networks for Sentence Classification
http://arxiv.org/pdf/1408.5882v2.pdf

Much of the code is modified from
- deeplearning.net (for ConvNet classes)
- https://github.com/mdenil/dropout (for dropout)
- https://groups.google.com/forum/#!topic/pylearn-dev/3QbKtCumAW4 (for Adadelta)
"""
import cPickle
import numpy as np
from collections import defaultdict, OrderedDict
import theano
import theano.tensor as T
import re
import warnings
import sys
import pandas as pd
import logging
import math
from conv_net_classes import LeNetConvPoolLayer
from conv_net_classes import MLPDropout

warnings.filterwarnings("ignore")   

logger = logging.getLogger('myapp')
hdlr = logging.FileHandler('log')
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
hdlr.setFormatter(formatter)
logger.addHandler(hdlr) 
logger.setLevel(logging.WARNING)

#different non-linearities
def ReLU(x):
    y = T.maximum(0.0, x)
    return(y)
def Sigmoid(x):
    y = T.nnet.sigmoid(x)
    return(y)
def Tanh(x):
    y = T.tanh(x)
    return(y)
def Iden(x):
    y = x
    return(y)
       
       

def build_model(U,
                img_h1,
                img_h2,
                img_w=100, 
                x1_filter_hs=[1,2,3],
                x2_filter_hs=[1,2,3],
                hidden_units=[100,2], 
                dropout_rate=0.5,
                batch_size=50, 
                conv_non_linear="relu",
                activation=Iden,
                sqr_norm_lim=9,
                non_static=True):
    """
    Train a simple conv net
    img_h1 = x1 length (padded where necessary)
    img_h2 = x2 length (padded where necessary)
    img_w = word vector length (300 for word2vec)
    filter_hs = filter window sizes    
    hidden_units = [x,y] x is the number of feature maps (per filter window), and y is the penultimate layer
    sqr_norm_lim = s^2 in the paper
    lr_decay = adadelta decay parameter
    """    
    rng = np.random.RandomState(3435)
    filter_w = img_w
    feature_maps = hidden_units[0]
    x1_filter_shapes = []
    x2_filter_shapes = []
    pool_x1_sizes = []
    pool_x2_sizes = []
    for filter_h in x1_filter_hs:
        x1_filter_shapes.append((feature_maps, 1, filter_h, filter_w))
        pool_x1_sizes.append((img_h1-filter_h+1, img_w-filter_w+1))
    for filter_h in x2_filter_hs:
        x2_filter_shapes.append((feature_maps, 1, filter_h, filter_w))
        pool_x2_sizes.append((img_h2-filter_h+1, img_w-filter_w+1))
    parameters = [("image x1 shape",img_h1,img_w), ("image x2 shape",img_h2,img_w), ("x1 filter shape",x1_filter_shapes), ("x2 filter shape",x2_filter_shapes), ("pool x1 size", pool_x1_sizes), ("pool x2 size", pool_x2_sizes), ("hidden_units",hidden_units),
                  ("dropout", dropout_rate), ("batch_size",batch_size),("non_static", non_static),
                    ("conv_non_linear", conv_non_linear), 
                    ("sqr_norm_lim",sqr_norm_lim)]
    print parameters
    logger.error("start")
    logger.error('Records: %s', parameters)

    
    #define model architecture
    x1 = T.imatrix('x1')
    x2 = T.imatrix('x2')
    y = T.ivector('y')
    Words = theano.shared(value = U, name = "Words")
    
    
    layer0_x1_input = Words[x1.flatten()].reshape((x1.shape[0],1,x1.shape[1],Words.shape[1]))
    layer0_x2_input = Words[x2.flatten()].reshape((x2.shape[0],1,x2.shape[1],Words.shape[1]))
    
    conv_layers = []
    conv_layers1 = []
    conv_layers2 = []

    x1_layer1_inputs = []
    x2_layer1_inputs = []
    for i in xrange(len(x1_filter_hs)):
        x1_conv_layer = LeNetConvPoolLayer(rng, input=layer0_x1_input,image_shape=(batch_size, 1, img_h1, img_w),
                                filter_shape=x1_filter_shapes[i], poolsize=pool_x1_sizes[i], non_linear=conv_non_linear)
        x1_layer1_input = x1_conv_layer.output.flatten(2)
        conv_layers1.append(x1_conv_layer)
        x1_layer1_inputs.append(x1_layer1_input)
    for i in xrange(len(x2_filter_hs)):
        x2_conv_layer = LeNetConvPoolLayer(rng, input=layer0_x2_input,image_shape=(batch_size, 1, img_h2, img_w),
                                filter_shape=x2_filter_shapes[i], poolsize=pool_x2_sizes[i], non_linear=conv_non_linear)
        x2_layer1_input = x2_conv_layer.output.flatten(2)
        conv_layers2.append(x2_conv_layer)
        x2_layer1_inputs.append(x2_layer1_input)
    
    
    
    x2_layer1_inputs += x1_layer1_inputs
    layer1_input = T.concatenate(x2_layer1_inputs, 1)
    hidden_units[0] = feature_maps * (len(x2_filter_hs) + len(x1_filter_hs))
    
    conv_layers = conv_layers1 + conv_layers2
    
    #x1_layer1_input = T.concatenate(x1_layer1_inputs,1)
    #x2_layer1_input = T.concatenate(x2_layer1_inputs,1)
    #outer_prod = x1_layer1_input.dimshuffle(0,1,'x') * x2_layer1_input.dimshuffle(0,'x',1)
    #layer1_input = outer_prod.flatten(2)
    #hidden_units[0] = feature_maps*len(x1_filter_hs) * feature_maps*len(x2_filter_hs)
    
    #layer1_input = x1_layer1_input * x2_layer1_input
    classifier = MLPDropout(rng, input=layer1_input, layer_sizes=hidden_units, activations=[activation], dropout_rates=[dropout_rate])
    
    return x1, x2, y, Words, conv_layers, classifier



def test_conv_net(output_file,test_data,
                U,
                params,
                filter_hs_1=[1,2,3],
                filter_hs_2=[1,2,3],
                conv_non_linear="relu",
                hidden_units=[100,2],
                shuffle_batch=True, 
                non_static=True,
                activation=Iden,
                sqr_norm_lim=9,
                batch_size=1000,
                dropout_rate=0.5):
    
    img_h1 = len(test_data[0][0])
    img_h2 = len(test_data[1][0])
    print test_data[0].shape
    print "img_h1=" + str(img_h1)
    print "img_h2=" + str(img_h2)
    img_w=100
    x1, x2, y, Words, conv_layers, classifier = build_model(
                U,
                img_h1,
                img_h2,
                img_w, 
                filter_hs_1,
                filter_hs_2,
                hidden_units=hidden_units, 
                dropout_rate=dropout_rate,
                batch_size=batch_size, 
                conv_non_linear=conv_non_linear,
                activation=Iden,
                sqr_norm_lim=sqr_norm_lim,
                non_static=non_static)
    
    
    ### 1.1 parameters for classifier
    W = params.pop(0)
    b = params.pop(0)
    classifier.params[0].set_value(W.get_value())
    classifier.params[1].set_value(b.get_value())
    
    ### 1.2 parameters for conv_layers
    for conv_layer in conv_layers:
        W = params.pop(0)
        b = params.pop(0)
        conv_layer.W.set_value(W.get_value())
        conv_layer.b.set_value(b.get_value())
    
    ### 2. organize data
    '''
    extra_data_num = 0
    n_batches = test_data.shape[0]/batch_size
    if test_data.shape[0] % batch_size > 0:
        extra_data_num = batch_size - test_data.shape[0] % batch_size
        extra_data = test_data[:extra_data_num]
        test_set=np.append(test_data,extra_data,axis=0)
    else:
        test_set = test_data

    test_set_x, test_set_y = shared_dataset((test_set[:,:img_h], test_set[:,-1]))

    ### 3. set up eval function and evaluate
    index = T.lscalar()
    test_model = theano.function([index], classifier.errors(y),
                                givens={
                                       x: test_set_x[index * batch_size: (index + 1) * batch_size],
                                       y: test_set_y[index * batch_size: (index + 1) * batch_size]})
        
        
    test_losses = [test_model(i) for i in xrange(n_batches)]
    print 'extra data size = ' + str(extra_data_num)
    if extra_data_num > 0:
        del test_losses[test_data.shape[0]-extra_data_num:test_data.shape[0]-1]
    test_perf = 1- np.mean(test_losses)
    print('test perf %f %%' % (test_perf * 100.))
    logger.error('test perf %f %%' % (test_perf * 100.))
    '''
        
    minibatches = []
    minibatch_start = 0
    '''
    for i in range(test_data[0].shape[0] // batch_size):
        minibatches.append((minibatch_start,minibatch_start+batch_size))
        minibatch_start += batch_size
    if (minibatch_start != test_data[0].shape[0]):
        minibatches.append((minibatch_start,test_data[0].shape[0]))
    '''    
    for i in range(test_data[0].shape[0] // batch_size):
        minibatches.append((minibatch_start,minibatch_start+batch_size))
        minibatch_start += batch_size
    if (minibatch_start != test_data[0].shape[0]):
        fake_size = batch_size + minibatch_start - test_data[0].shape[0]
        
        target = test_data[0]
        situation = test_data[1]
        labels = test_data[2]
                              
        extra_data_target = target[0:fake_size]
        extra_data_situation = situation[0:fake_size]
        extra_data_labels = labels[0:fake_size]
        
     #   extra_data_target = np.array(extra_data_target,dtype="int")
     #   extra_data_situation = np.array(extra_data_situation,dtype="int")
     #   extra_data_labels = np.array(extra_data_labels,dtype="int")
        
     #   extra_data = extra_data_target,extra_data_situation,extra_data_labels
        
        fake_data = np.zeros((fake_size, img_h1+1)),np.zeros((fake_size, img_h2+1))
        minibatches.append((minibatch_start,minibatch_start+batch_size))
        
        target = np.concatenate((target,extra_data_target),axis=0)
        situation = np.concatenate((situation,extra_data_situation),axis=0)
        labels = np.concatenate((labels,extra_data_labels),axis=0)

        test_data = [ target, situation, labels ]
        #new_data = [(x+dx, y+dy, z+dz) for x,y,z in test_data for dx,dy,dz in extra_data]

        
        #test_data = np.concatenate((test_data, extra_data), axis=0)
        
        
    
    #test_set_x1,  test_set_x2, test_set_y = shared_dataset((test_data[:,:img_h], test_data[:,-1]))
    
    test_set_x1, test_set_x2, test_set_y = shared_dataset(test_data)
    
    
    ### 3. set up eval function and evaluate
    s_idx = T.iscalar()
    e_idx = T.iscalar()
    test_model = theano.function([s_idx,e_idx], classifier.errors(y),
                                givens={
                                       x1: test_set_x1[s_idx : e_idx],
                                       x2: test_set_x2[s_idx : e_idx],
                                       y: test_set_y[s_idx : e_idx]})
    
    test_result = theano.function([s_idx,e_idx], classifier.layers[-1].y_pred,
                                givens={
                                       x1: test_set_x1[s_idx : e_idx],
                                       x2: test_set_x2[s_idx : e_idx]#,
                                       })
    
    
    losses = [test_model(minibatches[i][0], minibatches[i][1]) for i in xrange(len(minibatches))]
    

    test_losses = 0
    for i in xrange(len(losses)):
        test_losses += losses[i] * (minibatches[i][1]-minibatches[i][0])
    test_losses /= test_data[0].shape[0]
    test_perf = 1- test_losses
    print('test size ' + str(test_data[0].shape[0]))
    print('test perf %f %%' % (test_perf * 100.))
    logger.error('test perf %f %%' % (test_perf * 100.))

    

    test_preds = [test_result(minibatches[i][0], minibatches[i][1]) for i in xrange(len(minibatches))]
    with open(output_file, 'wb') as fout:
        for pred in test_preds:
            for p in pred:
                fout.write(str(p) + '\n')

    
def train_conv_net(train_data,
                model_file,
                U,
                x1_filter_hs = [1,2,3],
                x2_filter_hs = [1,2,3],
                hidden_units=[100,2], 
                batch_size=1000, 
                img_w=100, 
                dropout_rate=0.5,
                shuffle_batch=True,
                n_epochs=25, 
                lr_decay = 0.95,
                conv_non_linear="relu",
                activation=Iden,
                sqr_norm_lim=9,
                non_static=True):

    img_h1 = len(train_data[0][0])
    img_h2 = len(train_data[1][0])
    print train_data[0].shape
    print "img_h1=" + str(img_h1)
    print "img_h2=" + str(img_h2)

    x1, x2, y, Words, conv_layers, classifier = build_model(
                U,
                img_h1,
                img_h2,
                img_w=img_w, 
                x1_filter_hs=x1_filter_hs,
                x2_filter_hs=x2_filter_hs,
                hidden_units=hidden_units, 
                dropout_rate=dropout_rate,
                batch_size=batch_size, 
                conv_non_linear=conv_non_linear,
                activation=activation,
                sqr_norm_lim=sqr_norm_lim,
                non_static=non_static)
    index = T.lscalar()
        
    # this is to make sure that the first word is always zero (when non-static word embedding)
    #zero_vec_tensor = T.vector()
    #zero_vec = np.zeros(img_w, dtype=theano.config.floatX)
    #set_zero = theano.function([zero_vec_tensor], updates=[(Words, T.set_subtensor(Words[0,:], zero_vec_tensor))])



    #define parameters of the model and update functions using adadelta
    params = classifier.params
    for conv_layer in conv_layers:
        params += conv_layer.params
    if non_static:
        #if word vectors are allowed to change, add them as model parameters
        params += [Words]
    cost = classifier.negative_log_likelihood(y)
    dropout_cost = classifier.dropout_negative_log_likelihood(y)
    grad_updates = sgd_updates_adadelta(params, dropout_cost, lr_decay, 1e-6, sqr_norm_lim)
    
    
    #shuffle dataset and assign to mini batches. if dataset size is not a multiple of mini batches, replicate 
    #extra data (at random)
    # !! currently assume input is randomized !!
    np.random.seed(3435)

    n_batches = int(math.ceil(train_data[0].shape[0]/float(batch_size)))
    n_train_batches = int(np.round(n_batches*0.9))
    print 'n_batches: ', n_batches
    print 'n_train_batches: ', n_train_batches
    #for data in train_data:
    #    print data.shape 
    train_set = [data[:n_train_batches*batch_size] for data in train_data]
    val_set = [data[n_train_batches*batch_size:] for data in train_data]
    
    if val_set[0].shape[0] % batch_size > 0:
        extra_data_num = batch_size - val_set[0].shape[0] % batch_size
        new_set = [np.append(data, data[:extra_data_num],axis=0) for data in val_set]
        val_set = new_set
    #print 'train size =', train_set.shape, ' val size =', val_set.shape 

    
    
    train_set_x1, train_set_x2, train_set_y = shared_dataset(train_set)
    val_set_x1, val_set_x2, val_set_y = shared_dataset(val_set)    
    """
    test_model = theano.function([index], classifier.errors(y),
                                 givens={
                                         x1: train_set_x1[index * batch_size: (index + 1) * batch_size],
                                         x2: train_set_x2[index * batch_size: (index + 1) * batch_size],
                                         y: train_set_y[index * batch_size: (index + 1) * batch_size]})
    """
    
    
    n_val_batches = n_batches - n_train_batches
    val_model = theano.function([index], classifier.errors(y),
                                givens={
                                        x1: val_set_x1[index * batch_size: (index + 1) * batch_size],
                                        x2: val_set_x2[index * batch_size: (index + 1) * batch_size],
                                        y: val_set_y[index * batch_size: (index + 1) * batch_size]})

    #compile theano functions to get train/val/test errors
    test_model = theano.function([index], classifier.errors(y),
                                 givens={
                                         x1: train_set_x1[index * batch_size: (index + 1) * batch_size],
                                         x2: train_set_x2[index * batch_size: (index + 1) * batch_size],
                                         y: train_set_y[index * batch_size: (index + 1) * batch_size]})
    train_model = theano.function([index], cost, updates=grad_updates,
                                  givens={
                                          x1: train_set_x1[index * batch_size: (index + 1) * batch_size],
                                          x2: train_set_x2[index * batch_size: (index + 1) * batch_size],
                                          y: train_set_y[index * batch_size: (index + 1) * batch_size]})
    
    #start training over mini-batches
    print '... training'
    epoch = 0
    best_val_perf = 0
    val_perf = 0
    cost_epoch = 0    
    
    
    while (epoch < n_epochs):
        epoch = epoch + 1
        if shuffle_batch:
            j = 1
            for minibatch_index in np.random.permutation(range(n_train_batches)):
                cost_epoch = train_model(minibatch_index)
                #set_zero(zero_vec)
                #print ("j=%i" % j)
                #j+=1
                """
                train_losses = [test_model(i) for i in xrange(n_train_batches)]
                train_perf = 1 - np.mean(train_losses)
                
                val_losses = [val_model(i) for i in xrange(n_val_batches)]
                val_perf = 1- np.mean(val_losses)                        
                print('minibatch %i, val perf %f' % (minibatch_index, val_perf*100.))
                logger.info('minibatch %i, val perf %f' % (minibatch_index, val_perf*100.))
                """
        else:
            for minibatch_index in xrange(n_train_batches):
                cost_epoch = train_model(minibatch_index)  
                #set_zero(zero_vec)
        #train_losses = [test_model(i) for i in xrange(n_train_batches)]
        #train_perf = 1 - np.mean(train_losses)
        val_losses = [val_model(i) for i in xrange(n_val_batches)]
        val_perf = 1- np.mean(val_losses)
        print('epoch %i, val perf %f %%' % (epoch, val_perf*100.))
        logger.error('epoch %i, val perf %f %%' % (epoch, val_perf*100.))
        
        if val_perf >= best_val_perf:
            best_val_perf = val_perf
            fin = open(model_file, "wb")
            cPickle.dump(params, fin)
            
    
    #cPickle.dump(classifier, open(model_file, "wb"), protocol=cPickle.HIGHEST_PROTOCOL)
    
    return best_val_perf

    


def shared_dataset(data, borrow=True):
    """ Function that loads the dataset into shared variables

    The reason we store our dataset in shared variables is to allow
    Theano to copy it into the GPU memory (when code is run on GPU).
    Since copying data into the GPU is slow, copying a minibatch everytime
    is needed (the default behaviour if the data is not in a shared
    variable) would lead to a large decrease in performance.
    """
    data_x1, data_x2, data_y = data
    shared_x1 = theano.shared(np.asarray(data_x1,
                                               dtype='int32'),
                                 borrow=borrow)
    shared_x2 = theano.shared(np.asarray(data_x2,
                                               dtype='int32'),
                                 borrow=borrow)
    shared_y = theano.shared(np.asarray(data_y,
                                               dtype='int32'),
                                 borrow=borrow)
    return shared_x1, shared_x2, shared_y
        
def sgd_updates_adadelta(params,cost,rho=0.95,epsilon=1e-6,norm_lim=9,word_vec_name='Words'):
    """
    adadelta update rule, mostly from
    https://groups.google.com/forum/#!topic/pylearn-dev/3QbKtCumAW4 (for Adadelta)
    """
    updates = OrderedDict({})
    exp_sqr_grads = OrderedDict({})
    exp_sqr_ups = OrderedDict({})
    gparams = []
    for param in params:
        empty = np.zeros_like(param.get_value())
        exp_sqr_grads[param] = theano.shared(value=as_floatX(empty),name="exp_grad_%s" % param.name)
        exp_sqr_ups[param] = theano.shared(value=as_floatX(empty), name="exp_grad_%s" % param.name)
        gp = T.grad(cost, param)
        gparams.append(gp)
    for param, gp in zip(params, gparams):
        exp_sg = exp_sqr_grads[param]
        exp_su = exp_sqr_ups[param]
        up_exp_sg = rho * exp_sg + (1 - rho) * T.sqr(gp)
        updates[exp_sg] = up_exp_sg
        step =  -(T.sqrt(exp_su + epsilon) / T.sqrt(up_exp_sg + epsilon)) * gp
        updates[exp_su] = rho * exp_su + (1 - rho) * T.sqr(step)
        stepped_param = param + step
        '''
        if (param.get_value(borrow=True).ndim == 2) and (param.name!='Words'):
            col_norms = T.sqrt(T.sum(T.sqr(stepped_param), axis=0))
            desired_norms = T.clip(col_norms, 0, T.sqrt(norm_lim))
            scale = desired_norms / (1e-7 + col_norms)
            updates[param] = stepped_param * scale
        else:
            updates[param] = stepped_param
        '''
        updates[param] = stepped_param
    return updates 

def as_floatX(variable):
    if isinstance(variable, float):
        return np.cast[theano.config.floatX](variable)

    if isinstance(variable, np.ndarray):
        return np.cast[theano.config.floatX](variable)
    return theano.tensor.cast(variable, theano.config.floatX)
    


def word_2_index(word, word_idx_map):
    if word in word_idx_map:
        return word_idx_map[word]
    else:
        return 0

def index_x1(x1_word, sent, word_idx_map, filter_h=3):
    """
    Transforms sentence into a list of indices. Pad with zeroes.
    """
    x = []
    words = sent.split()
    position = words.index(x1_word)

    pad = filter_h - 1
    for i in xrange(pad):
        j = position - i - 1
        if j < 0:
            x.insert(0, 0)
        else:
            wi = word_2_index(words[j], word_idx_map)
            x.insert(0, wi)
            
    x.append(word_2_index(x1_word, word_idx_map))
    
    for i in xrange(pad):
        j = position + i + 1
        if j >= len(words):
            x.append(0)
        else:
            wi = word_2_index(words[j], word_idx_map)
            x.append(wi)
    return x

def index_x2(sent, word_idx_map, max_l=30, filter_h=3):
    """
    Transforms sentence into a list of indices. Pad with zeroes.
    """
    x = []
    pad = filter_h - 1
    for i in xrange(pad):
        x.append(0)
    words = sent.split()
    for word in words :
        
        if len(x) >= max_l+2*pad :
            break
        
        if word in word_idx_map:
            x.append(word_idx_map[word])
        else:
            x.append(1) # unknown word
    while len(x) < max_l+2*pad:
        x.append(0)
    
    return x


def make_idx_data(data, word_idx_map, max_x1,max_x2, x1_filter_h, x2_filter_h):
    """
    Transforms sentences into a 2-d matrix.
    """
    x1 = []
    x2 = []
    y = []
    for query in data:
        idx_x1 = index_x2(query["x1"], word_idx_map,max_x1, x1_filter_h)
        idx_x2 = index_x2(query["x2"], word_idx_map, max_x2, x2_filter_h)
        x1.append(idx_x1)
        x2.append(idx_x2)
        y.append(query['y'])
        
    x1_data = np.array(x1,dtype="int")
    x2_data = np.array(x2,dtype="int")
    y_data = np.array(y,dtype="int")
    return [x1_data, x2_data, y_data]

  
   
   
def test(fold):
    
    train_file = './data/output/context/pkl/2_cnn/' + 'tweet.SARCNOSARC.CONTEXT.prev.current.TRAIN.' + fold + '.pkl'
    model_file = './data/models/context/pkl/2_cnn/' + 'tweet.SARCNOSARC.CONTEXT.prev.current.TRAIN.model.' + fold + '.pkl'
    test_file = './data/output/context/pkl/2_cnn/' + 'tweet.SARCNOSARC.CONTEXT.prev.current.TEST.' + fold + '.pkl'
    output_file = './data/output/context/pred/2_cnn/'  + '.2cnn.0522.' + fold + '.pred'

    
    batch_size = 25
    x1_filter_hs = [1,2,3]
    x2_filter_hs = [1,2,3]
    hidden_units = [100,2]
    non_static = False

    params = cPickle.load(open(model_file,'rb'))
    test_data = cPickle.load(open(test_file,'rb'))
    x = cPickle.load(open(train_file,"rb"))
    _, W, word_idx_map, max_x1, max_x2 = x[0], x[1], x[2], x[3], x[4]
    U = W.astype(theano.config.floatX)
    idx_test_data = make_idx_data(test_data, word_idx_map, max_x1, max_x2, max(x1_filter_hs), max(x2_filter_hs))
    
    test_conv_net(output_file,idx_test_data,
                U,
                params,
                x1_filter_hs,
                x2_filter_hs,
                conv_non_linear="relu",
                hidden_units=hidden_units,
                shuffle_batch=True, 
                non_static=non_static,
                activation=Iden,
                sqr_norm_lim=9,
                batch_size=batch_size,
                dropout_rate=0.5)
    
    
def test_senti(target):
   
    
    train_file = './data/output/samelm/pkl/2_cnn/' + 'tweet.' + target + '.target.TRAIN'   + '.pkl'
    model_file = './data/models/samelm/2_cnn/' + 'tweet.' + target + '.target.TRAIN' + '.model.pkl'

    test_file = './data/output/samelm/pkl/senti/2_cnn/' + 'tweet.' + target + '.target.SENTI.BOTH.TEST' + '.pkl'
    output_file = './data/output/samelm/pred/senti/2_cnn/' + target + '.2cnn.0216.pred'

    
    batch_size = 25
    x1_filter_hs = [1,2,3]
    x2_filter_hs = [1,2,3]
    hidden_units = [100,2]
    non_static = False

    params = cPickle.load(open(model_file,'rb'))
    test_data = cPickle.load(open(test_file,'rb'))
    x = cPickle.load(open(train_file,"rb"))
    _, W, word_idx_map, max_x1, max_x2 = x[0], x[1], x[2], x[3], x[4]
    U = W.astype(theano.config.floatX)
    idx_test_data = make_idx_data(test_data, word_idx_map, max_x1, max_x2, max(x1_filter_hs), max(x2_filter_hs))
    
    test_conv_net(output_file,idx_test_data,
                U,
                params,
                x1_filter_hs,
                x2_filter_hs,
                conv_non_linear="relu",
                hidden_units=hidden_units,
                shuffle_batch=True, 
                non_static=non_static,
                activation=Iden,
                sqr_norm_lim=9,
                batch_size=batch_size,
                dropout_rate=0.5)
    
    
def train_senti(target):
    
    train_file = './data/output/samelm/pkl/senti/2_cnn/' + 'tweet.' + target + '.target.SENTI.BOTH.TRAIN'   + '.pkl'
    model_file = './data/models/samelm/senti/2_cnn/' + 'tweet.' + target + '.target.TRAIN' + '.model.pkl'

    
    batch_size = 25
    x1_filter_hs = [1,2,3]
    x2_filter_hs = [1,2,3]
    hidden_units = [100,2]
    non_static = True
    
    print "loading data...",
    logger.error("loading data...");
    

    x = cPickle.load(open(train_file,"rb"))
    train_data, W, word_idx_map, max_x1, max_x2 = x[0], x[1], x[2], x[3], x[4]
    print 'size=', len(W), len(W[0])
    img_w = len(W[0])
    print "data loaded!"
    U = W.astype(theano.config.floatX)
    
    print "max x1 length = " + str(max_x1)
    print "max x2 length = " + str(max_x2)

    execfile("./src/com/rutgers/cnn/conv_net_classes.py")
    
    #idx_train_data = make_idx_data(train_data, word_idx_map, max_x1, max_x2, max(x1_filter_hs), max(x2_filter_hs))
    idx_train_data = make_idx_data(train_data, word_idx_map, max_x1, max_x2, max(x1_filter_hs), max(x2_filter_hs))
    train_conv_net(idx_train_data,
                          model_file, 
                          U,
                          x1_filter_hs,
                          x2_filter_hs,
                          hidden_units,
                          batch_size,
                          img_w=img_w,
                          lr_decay=0.95,
                          conv_non_linear="relu",
                          shuffle_batch=True, 
                          n_epochs=25, 
                          sqr_norm_lim=9,
                          non_static=non_static,
                          dropout_rate=0.5)
 #   print "test perf: " + str(perf)

def train(fold):

    train_file = './data/output/context/pkl/2_cnn/' + 'tweet.SARCNOSARC.CONTEXT.prev.current.TRAIN.' + fold + '.pkl'
    model_file = './data/models/context/pkl/2_cnn/' + 'tweet.SARCNOSARC.CONTEXT.prev.current.TRAIN.model.' + fold + '.pkl'

    
    batch_size = 25
    x1_filter_hs = [1,2,3]
    x2_filter_hs = [1,2,3]
    hidden_units = [100,2]
    non_static = True
    
    print "loading data...",
    logger.error("loading data...");
    

    x = cPickle.load(open(train_file,"rb"))
    train_data, W, word_idx_map, max_x1, max_x2 = x[0], x[1], x[2], x[3], x[4]
    print 'size=', len(W), len(W[0])
    img_w = len(W[0])
    print "data loaded!"
    U = W.astype(theano.config.floatX)
    
    print "max x1 length = " + str(max_x1)
    print "max x2 length = " + str(max_x2)

    execfile("./src/com/rutgers/cnn/conv_net_classes.py")
    
    #idx_train_data = make_idx_data(train_data, word_idx_map, max_x1, max_x2, max(x1_filter_hs), max(x2_filter_hs))
    idx_train_data = make_idx_data(train_data, word_idx_map, max_x1, max_x2, max(x1_filter_hs), max(x2_filter_hs))
    train_conv_net(idx_train_data,
                          model_file, 
                          U,
                          x1_filter_hs,
                          x2_filter_hs,
                          hidden_units,
                          batch_size,
                          img_w=img_w,
                          lr_decay=0.95,
                          conv_non_linear="relu",
                          shuffle_batch=True, 
                          n_epochs=25, 
                          sqr_norm_lim=9,
                          non_static=non_static,
                          dropout_rate=0.5)
#    print "test perf: " + str(perf)

def listTargets():
    file = open('./data/config/targets.txt')
    targets = [ line.strip() for line in file.readlines()]
    return targets

   
if __name__=="__main__":
    
    folds = ['two', 'three', 'four', 'five']
    for fold in folds:
        train(fold)
        test(fold)
       # test_senti(target)

        
