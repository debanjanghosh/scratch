#this is for combining all input files for WMF/Glove/word2vec/ and filtering the odd outs
#all the files have different formats so need to check that
#first load the sarcastic file

import os
import codecs
from sets import Set

alldataList = []
allIdList = []

#first load all the targets
path = './data/config/'
file = 'topnames.txt'
reader = open(path + file)
lines = reader.readlines()
targetList = [line.strip() for line in lines]

reader.close()

print 'targets are loaded '

path = './data/input/sarcasm_tokens/'
file = 'tweet.sarcasm.filtered.031012015.lemma.emoji.targets.all'
reader = codecs.open(path + file,'r','utf-8')
lines = reader.readlines()
for line in lines:
	features = line.split('\t')
	if len(features) !=3:
		print 'error in feature set size'
		continue 
	
	
	id = features[1]
	message = features[2]
	
	if id in allIdList:
		continue
	allIdList.append(id)
	alldataList.append(message)
	
reader.close()

print 'sarcasms are loaded '


#now load the random tokens
path = './data/input/random_tokens/'
file = 'tweet.rnd.all.filtered.031012015.lemma.emoji.targets'
reader = codecs.open(path + file,'r','utf-8')
lines = reader.readlines()
for line in lines:
	features = line.split('\t')
	if len(features) !=3 :
		print 'error in feature set size'
		continue 
	id = features[1]
	message = features[2]
	
	if id in allIdList:
		continue
	allIdList.append(id)
	alldataList.append(message)
	
reader.close()

print 'random are loaded '


#now load the tokens from the positive file
#this file is formatted differently!

path = './data/input/positive_tokens/'
file = 'tweet.positive.all.nostem.02192015.tokens.lemma.emoji'
reader = codecs.open(path + file,'r','utf-8')
lines = reader.readlines()
for line in lines:
	features = line.split('\t')
	if len(features) !=2:
		print 'error in feature size'
		continue
	id = features[0]
	message = features[1]
	if id in allIdList:
		continue
	allIdList.append(id)
	features = message.split()
	for feature in features:
		feature = feature.lower()
		
		if feature in targetList:
			 alldataList.append(message)
			 break
			
reader.close()

print 'positives are loaded '

print 'the size of all data is: ' + len(alldataList)
alldataSet = Set(alldataList)

print 'the size of all unique data is: ' + len(alldataSet)

#now print
path = './data/input/'
file = 'tweet.all.03222015.tokens.lemma.emoji.wmf'
writer = codecs.open(path + file, 'w','utf-8')
for data in alldataSet:
	writer.write(data)
	writer.write('\n')


writer.close() 