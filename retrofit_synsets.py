#retroftting sysets for wordnets
# we will derive the derivatives here 
import numpy as np
import math

from copy import deepcopy

def loadWordVectors(word_vec_path,word_vec_file,vocabs):
    
    V = {}
    fileObject = open(word_vec_path + '/' + word_vec_file, 'r')
  
    num = 0
    vector_length = 0
    for line in fileObject:
        line = line.strip().lower()
        wv = line.split()
        word = wv[0]
        vector = wv[1:]
        vector_length = len(vector)
        
        if word not in vocabs:
            continue
        
        V[word] = np.zeros(vector_length, dtype=float)
        for index, vecVal in enumerate(vector):
            V[word][index] = float(vecVal)
            ''' normalize weight vector '''
        
        V[word] /= math.sqrt((V[word]**2).sum() + 1e-6)
        num+=1
        if num == 100:
            break
        
    print 'Vectors read from: '+ word_vec_path + '/' + word_vec_file
    return V,vector_length

def getIndex(vocabs,word):
    
    if word not in vocabs:
        vocabs.append(word)
    
    return vocabs.index(word)
    
        
def loadSynsetVectors(synset_path,synset_file,word_vectors):
    
    lexicon ={}
    V = {}
    W = {}
    vocabs = []
    terms_length = 30000 # hardcoded for the time being
    
    for line in open(synset_path + '/' + synset_file, 'r') :
        terms = line.lower().strip().split('\t')
        word = terms[0]
        synset = norm_synset_pos(terms[1])
        lemmas = norm(terms[2])
        hypernyms = norm_hypernym_pos(terms[3])
 #       hyponyms = norm_synset_pos(terms[4]) 

        # for the t ime being - add every synset and hypernym as the neighbors 
        index = getIndex(vocabs,word)
        




        synsets = synset,hypernyms
        olds = lexicon.get(word,[])
        olds.append(synsets)
        lexicon[word] = olds
    
    print 'lexicon read and synsets are set from: ' + synset_file
    return lexicon

def norm(terms):
    
    norm_terms = []
    for term in terms:
        norm_terms.append(term)
    
    return norm_terms

def  norm_synset_pos( term ):
    
    term = term.replace('synset','')
    term = term.replace('(','')
    term = term.replace(')', '')
    elements = term.split('.')
    norm_term = elements[0] + '.' + elements[1]
    return norm_term

def  norm_hypernym_pos( hypernyms ):
    
  #  print hypernyms
    norm_terms = []
    hypernyms = hypernyms[1:len(hypernyms)-1]
   
    if not hypernyms or len(hypernyms) == 0:
        return norm_terms
    
    terms = hypernyms.split(',')
    
    if not terms or len(terms) == 0:
        return norm_terms
    
    for term in terms:
        term = term.strip()
        term = term.replace('synset','')
        term = term.replace('(','')
        term = term.replace(')', '')
        
        elements = term.split('.')
        norm_term = elements[0] + '.' + elements[1]
        norm_terms.append(norm_term)
        
    return norm_terms

def getSiblings(synsets):
    siblings = []
    
    for synset in synsets:
        key = synset[0]
        siblings.append(key)
    
    return set(siblings)

def getSynsetVectorsSum(parent,lexicon_vectors,synset_vectors,num_siblings,vec_length):
    
    synsets = lexicon_vectors.get(parent,[])
    synset_siblings = getSiblings(synsets)
    
    synset_siblings_present = set(synset_vectors.keys()).intersection(synset_siblings)  
    
    embedding_sum = np.zeros(vec_length)
    num = 0.0
    for sibling in synset_siblings_present:
        vector = synset_vectors.get(sibling)
        embedding_sum = np.sum([embedding_sum, vector], axis=0) # vector sum (similar to Mitchell / Lapata but with predicted vectors)
            
    embedding_sum  = np.divide(embedding_sum,num_siblings)
    return embedding_sum
    


def retrofit_function(word_vectors,lexicon_vectors,vector_length):
    
    new_word_vectors = deepcopy(word_vectors)
    new_word_vocabs = set(new_word_vectors.keys())
    
    #we will  loop on the intersection of the vocab and words that have synsets
    new_word_vocabs_intersections = new_word_vocabs.intersection(set(lexicon_vectors.keys()))

    #for words in the intersection - initialize all the synsets with original vectors
    synsetVectors = {}
    synsetSiblings = {}
    synsetParents = {}
    for new_word_vocabs_intersection in new_word_vocabs_intersections:
        synsets = lexicon_vectors.get(new_word_vocabs_intersection,[])
        if len(synsets) == 0:
            continue
        for synset in synsets:
            key = synset[0]
            value = synset[1]
            synsetVectors[key] = new_word_vectors.get(new_word_vocabs_intersection)
            synsetSiblings[key] = float(len(synsets))
            synsetParents[key] = new_word_vocabs_intersection
    print 'synsets are initialized...'
    
    '''
    now we loop over the synsets
    the update equation has two parts. first part: we optimize how the synsets (set of j) effect the original vector (i)
    second part: how the synsets are affected by their hypernym vectors (set of n neighbors)
    '''
    alpha = 0.5 # the knob
    for synset in synsetVectors.keys():
        '''
            we first calculate the "first part"
            we need the number of synsets (siblings) for the availbele candidate synset 
            for instance for "bank.01" there are 17 siblings
        '''
        num_siblings  = synsetSiblings.get(synset) 
        parent = synsetParents.get(synset)
        
        sum_siblings_vectors = getSynsetVectorsSum(parent,lexicon_vectors,synsetVectors,num_siblings,vector_length)
        update_A_synset = alpha * (2.0/num_siblings) * ( np.subtract(sum_siblings_vectors,new_word_vectors.get(parent))  ) 
        
        print 'here'
        
        
    
    
    iterations = 10
    '''
    for iteration in range(iterations):
        for new_word_vocabs_intersection in new_word_vocabs_intersections:
            synsets = lexicon[new_word_vocabs_intersection]
            if not synsets or len(synsets) == 0:
                continue
            
            #we can work on this for words that have "vectors"
            synsets = synsets.intersection(new_word_vocabs)
            if len(synsets) == 0:
                continue
            
            #bring the derivatives
            
    '''        
        
def loadWords(synset_path,word_file):
    
    f = open(synset_path+'/'+word_file,'r')
    words = [ line.strip() for line in f.readlines()]
    return words

def main():
    
    #first load the word vectors (anything - glove for example )
    word_vec_path = '/home/z003ndhf/work_debanjan/data/glove_vectors/'
    word_vec_file = 'subwiki_L4D5_articles.glove.300.txt'
    synset_path = '/home/z003ndhf/work_debanjan/data/retrofit/'
    word_file = '30K.txt'
    vocabs  = loadWords(synset_path,word_file)
    word_vectors,vector_length = loadWordVectors(word_vec_path,word_vec_file,vocabs)

    
    synset_file = '30K_synsets.txt'
    synset_vectors = loadSynsetVectors(synset_path,synset_file)
    retrofit_function(word_vectors,synset_vectors,vector_length)
    
    

if __name__ == '__main__':
    main()