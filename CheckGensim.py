#check gensim

from gensim.models import word2vec
import logging
import nltk 



print 'gensim'
path = '/Users/dg513/work/eclipse-workspace/distrib-workspace/wordVectorLucene/data/'

targetList = []

def createTokenizedList():
    
    sentences = []
    reader = open(path + 'input/sarcasm/tweet.positiveEndHashtag.lm.28122014')
    lines = reader.readlines()
    
    for line in lines:
        tokens = nltk.word_tokenize(line)
        sentence = " ".join(tokens)
        sentences.append(sentence)
    
    return sentences


def createWordVector():
    logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)
    
 #   sentences = createTokenizedList()
    
# load up unzipped corpus from http://mattmahoney.net/dc/text8.zip
    sentences = word2vec.Text8Corpus(path + 'input/tweet.all.05252016.tokens.lemma.emoji.vector')
# train the skip-gram model; default window=5    
#sg = 1 skip gram sg = 0 cbow
    model = word2vec.Word2Vec(sentences, size=100,min_count=5,sg=1,window=10)
 
# pickle the entire model to disk, so we can load&resume training later
    model.save(path + 'model/tweet.all.05252016.sg.model')
# store the learned weights, in a format the original C tool understands
    model.save_word2vec_format(path + 'model/tweet.all.05252016.sg.model.bin', binary=True)
    
def onlineWordVector():
    model = word2vec.Word2Vec.load_word2vec_format(path + 'model/tweet.all.05032015.sg.model.bin', binary=True)
    

def loadWordVector():

    model = word2vec.Word2Vec.load_word2vec_format(path + 'model/tweet.all.04072015.sg.model.bin', binary=True)
   # print model['sarcasm']
  #  print model.most_similar(positive=['dentist','love'])
    positive = []
    negative = []
    print model.most_similar('sarcasm',positive,25)
  #  print model['morning']#.most_similar('morning',positive,100)

def loadTargetNames(path,file):

    reader = open(path + '/'+file)
    lines =reader.readlines()
    global targetList
    targetList = [line.strip() for line in lines]

def main():
   
    createWordVector()
   # loadWordVector()

main()
   