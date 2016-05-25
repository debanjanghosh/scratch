from gensim.models import word2vec
import logging
import os
import math
import numpy
import datetime
import codecs


#print 'gensim'
path = '/Users/dg513/work/eclipse-workspace/lucene-workspace/wordVectorLucene/data/'
#path = '/export/projects/sarcasm/vector/data/gensim/'


targetList = []
wordModelList = []

THRESHOLD = 0.85

def loadModel ( type ):
    
    if type == 'cbow':
        model = word2vec.Word2Vec.load_word2vec_format(path + 'model/tweet.all.03222015.cbow.model.bin', binary=True)
    if type == 'sg' :
        model = word2vec.Word2Vec.load_word2vec_format(path + 'model/tweet.all.04072015.sg.model.bin', binary=True)
    if type == 'bl' :
    	model = word2vec.Word2Vec.load_word2vec_format(path + 'model/GoogleNews-vectors-negative300.bin', binary=True)

def createTextFormatModel(configPath,dataList1,target,model, type):
    
 #  model = word2vec.Word2Vec.load_word2vec_format(path + 'model/tweet.all.04072015.sg.model.bin', binary=True)
  
    #first put -1.0 in all elements!
    rowLen = len(dataList1);
    if type == 'cbow':
        writer = codecs.open(configPath + '/' + 'tweet.all.05122015.cbow.model.bin.txt', 'w', 'utf-8')
    if type == 'sg':
        writer = codecs.open(configPath + '/' + 'tweet.all.05122015.sg.model.bin.txt', 'w', 'utf-8')
    if type == 'bl':
        writer = codecs.open(configPath + '/' + 'tweet.all.05122015.googlenews.model.bin.txt', 'w', 'utf-8')
   
   
    
    for index1 in range(0,rowLen):
        label1 = dataList1[index1][0]
        idMessage1 = dataList1[index1][1]
        id1 = idMessage1.split('_')[0]
        message1=idMessage1.split('_')[1]
        messageWordList1 = convertIntoCountMap(message1.split())
        createTextFormat(messageWordList1,target,writer)
    
    writer.close()

def convertIntoCountMap(words):    
    
    wordMap = {}
    wordPairList = []
    for word in words:
        old = wordMap.get(word)
        if old is None:
            old = 0
        wordMap[word] = old +1
    
    for word in wordMap.keys():
        pair = word,wordMap.get(word)
        wordPairList.append(pair)
    
    return wordPairList
  
def createTextFormat(gwordList,target,writer):
    for gwordElement in gwordList:
        gword = gwordElement[0]
        if gword in wordModelList:
            continue 
        try:
            gmodel = model[gword]
        except:
            continue
        wordModelList.append(gword)
        str = gword + '\t' + str(gmodel)
        writer.write(gword + '\t' + str(gmodel))
        writer.write('\n')



def loadTargetNames(path,file):

    reader = open(path + '/'+file)
    lines =reader.readlines()
    global targetList
    targetList = [line.strip() for line in lines]

def getDataList(path, dataList, target,dataType,exprType):

    #    tweet.SARCASM.excited.TRAIN
    file = 'tweet'+'.'+dataType+'.'+target+'.'+exprType
    
    reader = open(path + '/' + file )
    lines = reader.readlines()
    for line in lines:
        features = line.strip().split('\t')
        target = features[0]
        id = features[1]
        message = features[2]  
        if dataType == 'SARCASM':
            data = '1',id+'_'+message
        if dataType == 'NON_SARCASM':
            data = '0',id+'_'+message
       
        dataList.append(data)
    
    reader.close()
    return dataList
    
def main():
   
    sarcTrainingInputPath = './data/input/sarcasm_training/'
    randomTrainingInputPath  = './data/input/random_training/'
    
    sarcTestingInputPath = './data/input/sarcasm_testing/'
    randomTestingInputPath = './data/input/random_testing/'
    
    configPath = './data/config/'
    targets = 'topnames.txt'
    loadTargetNames(configPath,targets)
    loadModel('cbow')
    
    for target in targetList:
    
        print '%s' % (target) + ' is loaded '
         #now load the sarcasm traing files
        trainingSarcList = []
        trainingSarcList = getDataList(sarcTrainingInputPath,trainingSarcList,target,'SARCASM','TRAIN')
        print 'sarcasm training data is loaded'
        
        trainingNonSarcList = []
        trainingNonSarcList = getDataList(randomTrainingInputPath,trainingNonSarcList,target,'NON_SARCASM','TRAIN')
        print 'non sarcasm training data is loaded'
        
        trainingNonSarcList = []
        trainingNonSarcList = getDataList(randomTrainingInputPath,trainingNonSarcList,target,'NON_SARCASM','TRAIN')
        print 'non sarcasm training data is loaded'
      
       
        #now send all data for training
        #add all data
        for element in trainingNonSarcList:
            trainingSarcList.append(element)
        
        st = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        print 'before creating text format model: ' + st
        
        createTextFormatModel(configPath,trainingSarcList,target)
        print 'sarcasm/non sarcasm text format is ready'
        
        st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
        print 'after text format (training): ' + st
        
        
        testSarcList = []
        testSarcList = getDataList(sarcTestingInputPath,testSarcList,target,'SARCASM','TEST')
        print 'sarcasm testing is loaded'
        
        testNonSarcList = []
        testNonSarcList = getDataList(randomTestingInputPath,testNonSarcList,target,'NON_SARCASM','TEST')
        print 'non sarcasm testing is loaded'
        
        for element in testNonSarcList:
            testSarcList.append(element)
        
        st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
        print 'before creating text format model: ' + st
       
        
        createTextFormatModel(configPath,testSarcList,target)
        print 'sarcasm/non sarcasm test kernel matrix is ready'
        st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
        print 'after text format  (testing): ' + st
        

main()
   