import numpy as np
import cPickle
from collections import defaultdict
import sys, re
import pandas as pd

from collections import Counter


"""
for input file, assume 1st column is label (int), and 2nd column is query (string)
"""
def read_data_file(data_file,target, max_l, is_train):
    queries = []
    ids = []
    change_count = 0
    
    with open(data_file, "r") as fin:
        for line in fin:
            line = line.strip()
            line = line.lower()
            [label, text] = line.split('\t')
            # initially - just do with the original msg only 
            # later do both 
            text = text.split('|||')[0] + ' ' + text.split('|||')[1]
            
            text = text.lower()
            words = text.split()
            # we dont keep the target word in the text - just to make it similar to the EMNLP2015 approach
            removes = []
            newWords = []
            if target is not None:
                for index, word in enumerate(words):
                    if word.lower() == target.lower() or word.lower() == '#'+ target.lower():
                        removes.append(index)
                    else:
                        newWords.append(word)
                
            newText = ' '.join(newWords)
            newText = newText.strip()    
            words = newText.split()
            
            # update vocab only when it is training data
            #note - we dont do it here anymore. Rather we keep a separate function for vocab building 
            '''
            if is_train == 1:
                for word in set(words):
                    vocab[word] += 1
            '''
            if len(words) > max_l:
                words = words[:max_l]
                change_count += 1
            datum = {"y": int(label),
                    "text": " ".join(words),
                    "num_words": len(words)}
            queries.append(datum)
            ids.append(label)
    
    print ("length more than %i: %i" % (max_l, change_count)) 
    
    
    return queries,ids

def read_all_data(allData, output_id_file,target, max_l, is_train):
    queries = []
    change_count = 0
    writer = open(output_id_file,'w')
    
    for line in allData:
        line = line.strip()
        line = line.lower()
        [label,text] = line.split('\t')
        text = text.split('|||')[0] # we are only using the current msg
        text = text.lower()
        words = text.split()
            # we dont keep the target word in the text - just to make it similar to the EMNLP2015 approach
        removes = []
        newWords = []
        if target is not None:
            for index, word in enumerate(words):
                if word.lower() == target.lower() or word.lower() == '#'+ target.lower():
                    removes.append(index)
                else:
                    newWords.append(word)
                
        newText = ' '.join(newWords)
        newText = newText.strip()    
        words = newText.split()
            
            # update vocab only when it is training data
            #note - we dont do it here anymore. Rather we keep a separate function for vocab building 
        '''
            if is_train == 1:
                for word in set(words):
                    vocab[word] += 1
        '''
        if len(words) > max_l:
            words = words[:max_l]
            change_count += 1
        datum = {"y": int(label),
                "text": " ".join(words),
                "num_words": len(words)}
        queries.append(datum)
        writer.write(label)
        writer.write('\n')
    
    print ("length more than %i: %i" % (max_l, change_count)) 
    
    writer.close()
    
    return queries

"""
for input file, assume 1st column is label (int), and 2nd column is query (string)
"""
def read_data(data, target, vocab, max_l, is_train):
    queries = []
    change_count = 0
    
    for line in data:
            line = line.strip()
            line = line.lower()
            [label, _, _, text] = line.split('\t');
            text = text.lower()
            
            # we dont keep the target word in the text - just to make it similar to the EMNLP2015 approach
            if target is not None:
                text = text.replace('#' +target, " ")
                text = text.replace(target, " ")
                
            words = text.split()
            # update vocab only when it is training data
            if is_train == 1:
                for word in set(words):
                    vocab[word] += 1
            
            if len(words) > max_l:
                words = words[:max_l]
                change_count += 1
            datum = {"y": int(label),
                    "text": " ".join(words),
                    "num_words": len(words)}
            queries.append(datum)
    
    print ("length more than %i: %i" % (max_l, change_count)) 
    
    return queries

    
    
def get_W(word_vecs):
    """
    Get word matrix. W[i] is the vector for word indexed by i
    """
    vocab_size = len(word_vecs)
    k = len(word_vecs.values()[0])
    word_idx_map = dict()
    W = np.zeros(shape=(vocab_size+2, k), dtype='float32')            
    
    W[0] = np.zeros(k) # 1st word is all zeros (for padding)
    W[1] = np.random.normal(0,0.17,k) # 2nd word is unknown word
    i = 2
    for word in word_vecs:
        W[i] = word_vecs[word]
        word_idx_map[word] = i
        i += 1
    return W, word_idx_map

def load_bin_vec(fname, vocab):
    """
    Loads 300x1 word vecs from Google (Mikolov) word2vec
    """
    word_vecs = {}
    with open(fname, "rb") as f:
        header = f.readline()
        vocab_size, layer1_size = map(int, header.split())
        print 'vocab size =', vocab_size, ' k =', layer1_size
        binary_len = np.dtype('float32').itemsize * layer1_size
        for line in xrange(vocab_size):
            word = []
            while True:
                ch = f.read(1)
                if ch == ' ':
                    word = ''.join(word)
                    break
                if ch != '\n':
                    word.append(ch)
            if word in vocab:
                word_vecs[word] = np.fromstring(f.read(binary_len), dtype='float32')  
            else:
                f.read(binary_len)
    return word_vecs


def train():
    
    train_file = '../data/query/tweet/train.shuf'
    output_file = train_file + '.pkl'
    max_l = 100
    
    np.random.seed(4321)
    
    print "loading training data...",
    vocab = defaultdict(float)
    train_data = read_data_file(train_file, vocab, max_l, 1)
    
    max_l = np.max(pd.DataFrame(train_data)["num_words"])
    print max_l
    
    print "data loaded!"
    print "vocab size: " + str(len(vocab))
    print "max sentence length: " + str(max_l)
    
    #w2v_file = "/Users/wguo/projects/nn/data/w2v/GoogleNews-vectors-negative300.bin"
    w2v_file = "../data/w2v/sdata.txt-vectors.bin"
    print "loading word2vec vectors...",
    w2v = load_bin_vec(w2v_file, vocab)
    
    print "word2vec loaded!"
    print "num words already in word2vec: " + str(len(w2v))
    #add_unknown_words(w2v, vocab)
    W, word_idx_map = get_W(w2v)
    cPickle.dump([train_data, W, word_idx_map, max_l], open(output_file, "wb"))
    print "dataset created!"

def train_senti(target,vocab):
    
    path = './data/input/sarcasm_senti_training/'
    
    train_file = path + 'tweet.' + target + '.target.SENTI.BOTH.TRAIN'
    output_train_file = './data/output/samelm/pkl/senti/1_cnn/' + 'tweet.' + target + '.target.SENTI.BOTH.TRAIN'   + '.pkl'
    output_train_id_file = './data/output/samelm/ids/senti/1_cnn/' + 'tweet.' + target + '.target.SENTI.BOTH.TRAIN' + '.id'
    max_l = 100
    
    np.random.seed(4321)
    
    print "loading training data...",
    train_data, train_id = read_data_file(train_file, target,max_l, 1)
    
    max_l = np.max(pd.DataFrame(train_data)["num_words"])
    print max_l
    
    print "data loaded!"
    print "vocab size: " + str(len(vocab))
    print "max sentence length: " + str(max_l)
    
    w2v_file = "./data/config/tweet.all.05032015.sg.model.bin"
    print "loading word2vec vectors...",
    w2v = load_bin_vec(w2v_file, vocab)
    
    print "word2vec loaded!"
    print "num words already in word2vec: " + str(len(w2v))
    #add_unknown_words(w2v, vocab)
    W, word_idx_map = get_W(w2v)
    np.random.shuffle(train_data)
    cPickle.dump([train_data, W, word_idx_map, max_l], open(output_train_file, "wb"))
    
    writer = open(output_train_id_file, 'w')
    for id in train_id:
        writer.write(id)
        writer.write('\n')
    
    writer.close()
    print "dataset created!"
 


def test_senti(target):
    
    path = './data/input/sarcasm_senti_testing/'
    test_file = path + 'tweet.' + target + '.target.SENTI.BOTH.TEST'
    output_test_file = './data/output/samelm/pkl/senti/1_cnn/' + 'tweet.' + target + '.target.SENTI.BOTH.TEST'   + '.pkl'
    output_test_id_file = './data/output/samelm/ids/senti/1_cnn/' + 'tweet.' + target + '.target.SENTI.BOTH.TEST' + '.id'

    max_l = 100
    test_data,test_ids = read_data_file(test_file,target, max_l, 0)
    cPickle.dump(test_data, open(output_test_file, "wb"))
    
    writer = open(output_test_id_file, 'w')
    for id in test_ids:
        writer.write(id)
        writer.write('\n')
    
    writer.close()
    
 
def train_test_allData(targets,vocab):
    
    path = '/Users/dg513/work/eclipse-workspace/sarcasm-workspace/SarcasmDetection/data/twitter_corpus/wsd/sentiment/samelm2/weiwei/'
    
    allTraining = []
    allTesting = []
    
    output_train_file = './data/output/samelm/' + 'tweet.' + 'ALLTARGETS' + '.target.TRAIN' + '.pkl'
    output_test_file = './data/output/samelm/' + 'tweet.' + 'ALLTARGETS' + '.target.TEST'   + '.pkl'
    
    output_train_id_file = './data/output/samelm/ids/1_cnn/' + 'tweet.' + 'ALLTARGETS' + '.target.TRAIN' + '.id'
    output_test_id_file = './data/output/samelm/ids/1_cnn/' + 'tweet.' + 'ALLTARGETS' + '.target.TEST' + '.id'

    
    max_l = 100
    w2v_file = "./data/config/tweet.all.05032015.sg.model.bin"
    print "loading word2vec vectors..."

    for target in targets:
        train_file = path + 'tweet.' + target + '.target.TRAIN'
        test_file = path + 'tweet.' + target + '.target.TEST'
        
        with open(train_file, "r") as fin:
            for line in fin:
                allTraining.append(line)
    
        with open(test_file, "r") as fin:
            for line in fin:
                allTesting.append(line)
    
    
    np.random.seed(4321)
    
    target = 'ALLTARGETS'
    print "loading training data...",
    train_data = read_all_data(allTraining, output_train_id_file,target, max_l, 1)
    test_data = read_all_data(allTesting, output_test_id_file,target, max_l, 0)
    cPickle.dump(test_data, open(output_test_file, "wb"))
    max_l = np.max(pd.DataFrame(train_data)["num_words"])
    print max_l
    
    print "data loaded!"
    print "vocab size: " + str(len(vocab))
    print "max sentence length: " + str(max_l)
    
    #w2v_file = "/Users/wguo/projects/nn/data/w2v/GoogleNews-vectors-negative300.bin"
    w2v_file = "./data/config/tweet.all.05032015.sg.model.bin"
    print "loading word2vec vectors...",
    w2v = load_bin_vec(w2v_file, vocab)
    
    print "word2vec loaded!"
    print "num words already in word2vec: " + str(len(w2v))
    #add_unknown_words(w2v, vocab)
    W, word_idx_map = get_W(w2v)
    np.random.shuffle(train_data)
    cPickle.dump([train_data, W, word_idx_map, max_l], open(output_train_file, "wb"))
    print "dataset created!"
        
    
def train_test(target,vocab):
    
    
    path = '/Users/dg513/work/eclipse-workspace/sarcasm-workspace/sarcasm_dialogue/Corpus/output/svm/5fold/' + target + '/'
    #train_file = '../data/query/sport/train.shuf'
    train_file = path + 'tweet.SARCNOSARC.CONTEXT.txt.train' + target
    test_file = path + 'tweet.SARCNOSARC.CONTEXT.txt.test' + target


    output_train_file = './data/output/context/pkl/1_cnn/' + 'tweet.SARCNOSARC.CONTEXT.prev.current.both'  +'.TRAIN.' + target + '.pkl'
    output_test_file = './data/output/context/pkl/1_cnn/' + 'tweet.SARCNOSARC.CONTEXT.prev.current.both'  + '.TEST.' + target + '.pkl'

    output_train_id_file = './data/output/context/ids/1_cnn/' + 'tweet.SARCNOSARC.CONTEXT.prev.current.both'  + '.TRAIN.' + target + '.id'
    output_test_id_file = './data/output/context/ids/1_cnn/' + 'tweet'  + '.TEST.' + target + 'both.id'

    max_l = 100
    
    np.random.seed(4321)
    
    print "loading training data...",
    
    train_data,train_id = read_data_file(train_file, target,max_l, 1)
    test_data,test_id = read_data_file(test_file, target,max_l, 0)
    cPickle.dump(test_data, open(output_test_file, "wb"))

    
    max_l = np.max(pd.DataFrame(train_data)["num_words"])
    print max_l
    
    print "data loaded!"
    print "vocab size: " + str(len(vocab))
    print "max sentence length: " + str(max_l)
    
    #w2v_file = "/Users/wguo/projects/nn/data/w2v/GoogleNews-vectors-negative300.bin"
    w2v_file = "./data/config/tweet.all.05032015.sg.model.bin"
    print "loading word2vec vectors...",
    w2v = load_bin_vec(w2v_file, vocab)
    
    print "word2vec loaded!"
    print "num words already in word2vec: " + str(len(w2v))
    #add_unknown_words(w2v, vocab)
    W, word_idx_map = get_W(w2v)
    np.random.shuffle(train_data)
    cPickle.dump([train_data, W, word_idx_map, max_l], open(output_train_file, "wb"))
    
    writer1 = open(output_train_id_file,'w')
    for id in train_id:
        writer1.write(id)
        writer1.write('\n')
        
    writer1.close()
    
    writer2 = open(output_test_id_file,'w')
    for id in test_id:
        writer2.write(id)
        writer2.write('\n')
        
    writer2.close()
    
    print "dataset created!"
    
def listTargets():
    file = open('./data/config/targets.txt')
    targets = [ line.strip() for line in file.readlines()]
    return targets

def create_all_vocab(targets):
    cutoff = 5
    vocab = defaultdict(float)
    path = '/Users/dg513/work/eclipse-workspace/sarcasm-workspace/SarcasmDetection/data/twitter_corpus/wsd/sentiment/samelm2/weiwei/'

    allLines = []
    for target in targets:
        train_file = path + 'tweet.' + target + '.target.TRAIN'
        lines = open(train_file).readlines()
        allLines.extend(lines)
    
    raw = [process_line(l) for l in allLines ]
    cntx = Counter( [ w for e in raw for w in e ] )
    lst = [ x for x, y in cntx.iteritems() if y > cutoff ] + ["## UNK ##"]
    vocab = dict([ (y,x) for x,y in enumerate(lst) ])
    
    writer = open ('./data/config/all_vocabs_samelm_train_10.txt', 'w')
    for key in vocab:
        writer.write(key + '\t' + str(vocab.get(key)))
        writer.write('\n')
    
    writer.close()
    
    return vocab # (this is a dictionary of [word] = [position] which is fine since we are only bothered about the key of the dict.
   


def create_vocab():
    
    cutoff = 5
    vocab = defaultdict(float)
    path = './data/config/'
    train_file = path + 'only_context.unigrams.txt'
    
    lines = open(train_file).readlines()
    raw = [process_line(l) for l in lines ]
    lst = [ x for x, y in raw if y > cutoff ] + ["## UNK ##"]
    vocab = dict([ (y,x) for x,y in enumerate(lst) ])
    vocab['@touser']= 100 # just a default
    return vocab # (this is a dictionary of [word] = [position] which is fine since we are only bothered about the key of the dict.
   

def process_line(line):
    
     [word, count] = line.split('\t')
     return word,count

if __name__=="__main__":
    #train()
    #test()
  #  targets = listTargets()
   # vocab = create_all_vocab(targets)
  #  train_test_allData(targets,vocab)
  #  targets = ['always', 'amazing', 'attractive', 'awesome']
 #   targets = ['brilliant', 'shocked', 'favorite', 'cool', 'hot','excited','proud','awesome','attractive', 'interested' ]
    vocab = create_vocab()
    folds = ['one', 'two', 'three', 'four', 'five']
    for fold in folds:
        train_test(fold,vocab)
  #      train_senti(target,vocab)
   #     test_senti(target)
        
        