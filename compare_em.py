#embedding comparison
import numpy as np
import scipy
from sklearn.metrics.pairwise import cosine_similarity
def init():

    w2v_file = "./data/config/tweet.all.05032015.sg.model.bin"
    print "loading word2vec vectors...",
    w2v = load_bin_vec(w2v_file)
    
    check_em = w2v['hate']
    
    cosine_max = 0
    word_max = []
    for word in w2v:
        v_em = w2v[word]
        cosine = cosine_similarity(check_em,v_em)
        if cosine > cosine_max:
            cosine_max = cosine
            if cosine_max > 0.2:
                print word + '\t' + str(cosine)
            


def load_bin_vec(fname):
    """
    Loads 300x1 word vecs from Google (Mikolov) word2vec
    """
    word_vecs = {}
    with open(fname, "rb") as f:
        header = f.readline()
        vocab_size, layer1_size = map(int, header.split())
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
            word_vecs[word] = np.fromstring(f.read(binary_len), dtype='float32')  
           
    return word_vecs

def main():
    init()
    

if __name__ == '__main__':
    main()
   