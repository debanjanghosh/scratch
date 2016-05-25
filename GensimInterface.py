#check gensim

#import numpy
import os
import math
import numpy
import datetime
from gensim.models import word2vec
import logging
#import nltk 


global model


#from enum import Enum
#wsd_type_enum = Enum('WSD', 'SARCASM NOT_SARCASM')



#print 'gensim'
path = '/Users/dg513/work/eclipse-workspace/lucene-workspace/wordVectorLucene/data/'
#path = '/scratch/dg513/work/sarcasm/vector/data/gensim/'

targetList = []
contextContextSimMap = {}
glossGlossMap = {}
wordWordModelMap = {}
wordSelfSimMap = {}

THRESHOLD = 0.85

def convertIntoCountMap(words):    
    
    wordMap = {}
    wordPairList = []
    for word in words:
        #important filtering - we do not want the target word here 
        if word == 'null':
            continue
        
        old = wordMap.get(word)
        if old is None:
            old = 0
        wordMap[word] = old +1
    
    for word in wordMap.keys():
        pair = word,wordMap.get(word)
        wordPairList.append(pair)
    
    return wordPairList

def printHelloWorld():
    
    print 'hello world'

def loadGensimModel(type):
    
    global model
    if type == 'cbow':
        model = word2vec.Word2Vec.load_word2vec_format(path + 'model/tweet.all.03222015.cbow.model.bin', binary=True)
    if type == 'sg':
        model = word2vec.Word2Vec.load_word2vec_format(path + 'model/tweet.all.04072015.sg.model.bin', binary=True)

def computeSimViaWord2Vec(one,two):
    
    one_words = one.split()
    two_words = two.split()
    contextWordsList_one = convertIntoCountMap(one_words)
    contextWordsList_two = convertIntoCountMap(two_words)
    similarity = getKernelSimilarity(contextWordsList_one,contextWordsList_two)
    return similarity


def computeGlossSimViaWord2Vec(model,glosses,target,type,glossNonSarcScoreMap,contextWords):

    contextWordsList = convertIntoCountMap(contextWords,target)
  #  model = word2vec.Word2Vec.load_word2vec_format(path + 'model/tweet.all.04072015.sg.model.bin', binary=True)
    num = 0
    print 'total number of glosses: ' + str(len(glosses.keys()))
    for glossId in glosses.keys():
        gloss = glosses.get(glossId)
        glossWords = gloss.split()
        glossWordsList = convertIntoCountMap(glossWords,target)

     #   similarity = getSimilarity(glossWordsList,contextWordsList,model)
     #   similarity = getDirectSimilarity(glossWordsList,contextWordsList,model)

        
        #similarity = getTopSimilarity(glossWords,contextWords,model)
        similarity = getGreedyGreedyTopSimilarity(glossWordsList,contextWordsList,model)
        glossNonSarcScoreMap[glossId] = similarity
       # num = num + 1
     #   if num %10 == 0:
      #      print 'done: ' + str(num) 
       #     st = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        #    print 'after done: ' + st
            
    return glossNonSarcScoreMap

def getDirectSimilarity(gwordList,cwordList,model):   
    
    similarity = 0.0
    for gwordElement in gwordList:
        gword = gwordElement[0]
        gcount = gwordElement[1]
        try:
            gmodel = model[gword]
        except:
            continue
      
        for cwordElement in cwordList:
            cword = cwordElement[0]
            ccount = cwordElement[1]
            try:
                cmodel = model[cword]
            except:
                continue
             
            score = model.similarity(gword,cword)    
            #use a threshold?
            if score >= THRESHOLD :
                similarity = similarity + score * gcount * ccount
    
    return similarity

def getSimilarity(gwordList,cwordList,model):   
    
    similarity = 0.0
    for gwordElement in gwordList:
        gword = gwordElement[0]
        gcount = gwordElement[1]
        try:
            gmodel = model[gword]
        except:
            continue
        for cwordElement in cwordList:
            cword = cwordElement[0]
            ccount = cwordElement[1]
            try:
                cmodel = model[cword]
            except:
                continue
            
            score = getCosineScore(gword,gmodel,cword,cmodel)    
            
            #use a threshold?
            if score >= THRESHOLD :
                similarity = similarity + score * gcount * ccount
    
    return similarity

def getGreedyTopSimilarity(gwordList,cwordList,model):
    
    similarity = 0.0
    maxPosnList = []
    posnPosnSim = [[0 for x in range(len(cwordList))] for x in range(len(gwordList))] 
    
    index1 = 0
    for index1 in range(0,len(gwordList)):
        gword = gwordList[index1][0]
        try:
            gmodel = model[gword]
        except:
            continue
        maxSim = -1000 # it is okay - I dont think we can have a similar score less than this 
        for index2 in range(0,len(cwordList)):
            cword = cwordList[index2][0]
            try:
                cmodel = model[cword]
            except:
                continue
            score = getCosineScore(gword,gmodel,cword,cmodel)  
            posnPosnSim[index1][index2] = score #that is score all results!
        # choose the alignment greedily
    w1chosen = [False for x in range(len(gwordList))]
    w2chosen = [False for x in range(len(cwordList))]
        
    totalSim = 0
    maxSim = 2
    index1 = 0
    index2 = 0

    while True: 
        maxSim = -2
        for  i  in range(0, len(gwordList)) :
            if w1chosen[i] == True :
                continue
            for j in range(0,len(cwordList)) :
                if w2chosen[j] ==True:
                    continue
                if posnPosnSim[i][j] > maxSim :
                    maxSim = posnPosnSim[i][j];
                    index1 = i;
                    index2 = j;
           
        if maxSim >= THRESHOLD :
            w1chosen[index1] = True
            w2chosen[index2] = True 
            totalSim = totalSim + maxSim * gwordList[index1][1] * cwordList[index2][1] #*  - we can use "tf" val1[index1] * val2[index2];
        else :
            break
            
    return totalSim    

def getKernelSimilarity(gwordList,cwordList):
    
    
    similarity = 0.0
    maxPosnList = []
 #   posnPosnSim = [[0 for x in range(len(cwordList))] for x in range(len(gwordList))] 
    
    scoreMap = {}
    
 #   if len(gwordList) > 100:
  #      gwordList = gwordList[0:100]
  #  if len(cwordList) > 100:    
   #     cwordList = cwordList[0:10]

    index1 = 0
    for index1 in range(0,len(gwordList)):
        gword = gwordList[index1][0]
        try:
            gmodel = model[gword]
        except:
            continue

        for index2 in range(0,len(cwordList)):
            cword = cwordList[index2][0]
            try:
                cmodel = model[cword]
            except:
                continue
            score = model.similarity(gword,cword)
           # score = getCosineScore(gword,gmodel,cword,cmodel)  
       #     posnPosnSim[index1][index2] = score #that is score all results!
            
            candidates = scoreMap.get(score)
            if candidates is None:
                candidates = []
            pair = index1,index2
            candidates.append(pair)
            scoreMap[score] = candidates
    
    # choose the alignment greedily
    #sort the keys in descending order
    
    keysSorted = sorted(scoreMap,reverse=True)
    
    w1chosen = []
    w2chosen = []
    
    totalSim = 0
    maxSim = -1000
    for key in keysSorted:
        if key >=THRESHOLD:
            maxSim = key
            candidates = scoreMap.get(key)
           #each candidate is above the threshold!
            for candidate in candidates:
               #any element will do, right?
               row = candidate[0]
               column = candidate[1]
               #row or column - if anyone is true then we go to the next element
               if row in w1chosen :
                   continue
               if column in w2chosen :
                   continue
               
               totalSim = totalSim + maxSim * gwordList[row][1] * cwordList[column][1]
               w1chosen.append(row)
               w2chosen.append(column)
        else:
            break
          
    return totalSim    
  

     
   

def getGreedyGreedyTopSimilarity(gwordList,cwordList,model):
    
    similarity = 0.0
    maxPosnList = []
 #   posnPosnSim = [[0 for x in range(len(cwordList))] for x in range(len(gwordList))] 
    
    scoreMap = {}
    
 #   if len(gwordList) > 100:
  #      gwordList = gwordList[0:100]
  #  if len(cwordList) > 100:    
   #     cwordList = cwordList[0:10]

    index1 = 0
    for index1 in range(0,len(gwordList)):
        gword = gwordList[index1][0]
        try:
            gmodel = model[gword]
        except:
            continue

        for index2 in range(0,len(cwordList)):
            cword = cwordList[index2][0]
            try:
                cmodel = model[cword]
            except:
                continue
            score = model.similarity(gword,cword)
           # score = getCosineScore(gword,gmodel,cword,cmodel)  
       #     posnPosnSim[index1][index2] = score #that is score all results!
            
            candidates = scoreMap.get(score)
            if candidates is None:
                candidates = []
            pair = index1,index2
            candidates.append(pair)
            scoreMap[score] = candidates
    
    # choose the alignment greedily
    #sort the keys in descending order
    
    keysSorted = sorted(scoreMap,reverse=True)
    
    w1chosen = []
    w2chosen = []
    
    totalSim = 0
    maxSim = -1000
    for key in keysSorted:
        if key >=THRESHOLD:
            maxSim = key
            candidates = scoreMap.get(key)
           #each candidate is above the threshold!
            for candidate in candidates:
               #any element will do, right?
               row = candidate[0]
               column = candidate[1]
               #row or column - if anyone is true then we go to the next element
               if row in w1chosen :
                   continue
               if column in w2chosen :
                   continue
               
               totalSim = totalSim + maxSim * gwordList[row][1] * cwordList[column][1]
               w1chosen.append(row)
               w2chosen.append(column)
        else:
            break
          
    return totalSim    
  

     

def getTopSimilarity(gwords,cwords,model):   
    
    #for each gword - we only store the *highest* cword similarity
    #and then add that ---
    
    similarity = 0.0
    maxPosnList = []
    for index1 in range(0,len(gwords)):
        gword = gwords[index1]
        try:
            gmodel = model[gword]
        except:
            continue
        maxSim = -1000 # it is okay - I dont think we can have a similar score less than this 
        
        for index2 in range(0,len(cwords)):
            if index2 in maxPosnList: #no repeat of maximum similarity
                continue 
            cword = cwords[index2]
            try:
                cmodel = model[cword]
            except:
                continue
            score = getCosineScore(gword,gmodel,cword,cmodel)  
            if score > maxSim:
                maxSim = score
                try:
                    maxPosnList.pop(index1)
                except IndexError:    
                    #do nothing
                    pass 
                    
                maxPosnList.insert(index1,index2)
           #     maxPosnList.
              
        similarity = similarity + maxSim 
    
    return similarity


def getCosineScore(gword,gmodel,cword,cmodel):

    if gword == cword:
        return 1.0
    
    if gword in wordSelfSimMap.keys():
        gscore = wordSelfSimMap[gword]
    else:
        gscore = numpy.dot(gmodel,gmodel)
        wordSelfSimMap[gword] = gscore
        
    if cword in wordSelfSimMap.keys():
        cscore = wordSelfSimMap[cword]
    else:
        cscore = numpy.dot(cmodel,cmodel)
        wordSelfSimMap[cword] = cscore
   
    gscore = numpy.sqrt(gscore)
    cscore = numpy.sqrt(cscore)
    if gscore == 0 or cscore == 0 :
        return 0
    
    gcscore = numpy.dot(gmodel,cmodel)
    #normalized - cosine
    gcscore = gcscore/(gscore*cscore)
   #     check = numpy.dot(cmodel,gmodel)
    #    wordWordModelMap[gword + '_'+cword] = score
     #   wordWordModelMap[cword + '_'+gword] = score
        
    return gcscore

def loadWordVector():

    model = word2vec.Word2Vec.load_word2vec_format(path + 'model/tweet.all.03222015.cbow.model.bin', binary=True)
   # print model['sarcasm']
  #  print model.most_similar(positive=['dentist','love'])
    positive = []
    negative = []
    print model.most_similar('morning',positive,100)
    print model['morning']#.most_similar('morning',positive,100)

def loadUnigrams(path,file):
    
    unigramMap = {}
    reader = open(path + '/' + file)
    lines = reader.readlines()
    index = 0
    for line in lines:
        features = line.strip().split('\t')
        word = features[0]
        unigramMap[word] = index
        index = index + 1
    
    reader.close()
    return unigramMap

def loadTargetNames(path,file):

    reader = open(path + '/'+file)
    lines =reader.readlines()
    global targetList
    targetList = [line.strip() for line in lines]

def getGlossList(glossPath, glossSarcMap, target,type):

    file = 'tweet.'+type+'.'+target+'.glossdefn.wmf.txt'
    reader = open(glossPath + '/' + file )
    lines = reader.readlines()
    for line in lines:
        features = line.strip().split('\t')
        id = features[0]
        gloss = features[2] #note - this need to be changed because currently gloss defn and original tweets are together
        glossSarcMap[id] =gloss
    
    reader.close()
    return glossSarcMap
    

def loadTargetContextVectors(path,target,type):

    sarcPMIList = []
    file = 'tweet.' +type + '.'+target +'.context.txt.locpmi'
    reader = open(path + str(file))
    lines = reader.readlines()
    for line in lines:
        features = line.strip().split('\t')
        t = features[0]
        context = features[1]
        #sanity checing
        if t != target:
            print 'targets not matching: ' + target
            continue 
        
        sarcPMIList.append(context)
    
    reader.close()
    return sarcPMIList


def printGlossSimSarcTestFiles(path,target,type,glossSarcScoreMap,glossNonSarcScoreMap) :
    
    file = 'tweet.'+type +'.'+target+'.TEST.glossdefn.score'
    
    writer = open(path + '/' + file,'w')
  #  writer.write('test_data_id' + '\t' + 'sarc_sim' + '\t' + 'gloss_sim')
  #  writer.write('\n')
    
    for key in glossSarcScoreMap.keys():
        writer.write(type + '\t' + key + '\t' + 'SARCASM' + '\t'+str(glossSarcScoreMap[key]) + '\t' + 'NON_SARCASM' + '\t' + str(glossNonSarcScoreMap[key]))
        writer.write('\n')
    
    writer.close()
        
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

def createGloveFormat(configPath,dataList1,target):
    
    model = word2vec.Word2Vec.load_word2vec_format(path + 'model/tweet.all.03222015.cbow.model.bin', binary=True)
  #  model = word2vec.Word2Vec.load_word2vec_format(path + 'model/tweet.all.04072015.sg.model.bin', binary=True)
  
    #first put -1.0 in all elements!
    rowLen = len(dataList1);
    colLen = len(dataList2);
   # kernelScores = [[-1.0 for i in range(colLen)] for j in range(rowLen)] 
    
    kernelScores = numpy.zeros((rowLen,colLen))
    
#    for i in range(0,rowLen):
 #       for  j in range(0,colLen) :
  #          kernelScores[i][j] = -1.0
           
    for index1 in range(0,rowLen):
        label1 = dataList1[index1][0]
        idMessage1 = dataList1[index1][1]
        id1 = idMessage1.split('_')[0]
        message1=idMessage1.split('_')[1]
        messageWordList1 = convertIntoCountMap(message1.split())
        
        kernel_similarity_one = -1.0
        if id1 in idKernelMap.keys():
            kernel_similarity_one = idKernelMap[id1]
        else:
            kernel_similarity_one = getSimilarity(messageWordList1,messageWordList1,model)
            idKernelMap[id1] = kernel_similarity_one
    
   
def main():
   
    sarcTrainingInputPath = './data/input/sarcasm_training/'
    randomTrainingInputPath  = './data/input/random_training/'
    
    sarcTestingInputPath = './data/input/sarcasm_testing/'
    randomTestingInputPath = './data/input/random_testing/'
    
    configPath = './data/config/'
    targets = 'topnames.txt'
    loadTargetNames(configPath,targets)
    
    for target in targetList:
    
        if target != 'always':
            continue 
        print '%s' % (target) + ' is loaded '
         #now load the sarcasm traing files
        trainingSarcList = []
        trainingSarcList = getDataList(sarcTrainingInputPath,trainingSarcList,target,'SARCASM','TRAIN')
        print 'sarcasm training is loaded'
        
        trainingNonSarcList = []
        trainingNonSarcList = getDataList(randomTrainingInputPath,trainingNonSarcList,target,'NON_SARCASM','TRAIN')
        print 'non sarcasm training is loaded'
       
        #now send all data for training
        #add all data
        for element in trainingNonSarcList:
            trainingSarcList.append(element)
        
        st = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        print 'before creating glove version: ' + st
        
        createGloveFormat(configPath,trainingSarcList,target)
        
        testSarcList = []
        testSarcList = getDataList(sarcTestingInputPath,testSarcList,target,'SARCASM','TEST')
        print 'sarcasm testing is loaded'
        
        testNonSarcList = []
        testNonSarcList = getDataList(randomTestingInputPath,testNonSarcList,target,'NON_SARCASM','TEST')
        print 'non sarcasm testing is loaded'
        
        for element in testNonSarcList:
            testSarcList.append(element)
        
        st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
        print 'before testing: ' + st
       
        
        createTestKernelMatrix(kernelTestingPaths,testSarcList,trainingSarcList,target)
        print 'sarcasm/non sarcasm test kernel matrix is ready'
        st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
        print 'after testing: ' + st
        
 
main()
   