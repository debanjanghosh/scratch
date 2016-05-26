#output the # of the tweets with target seed words
import os
import codecs
from sets import Set
from nltk.corpus import wordnet as wn
from nltk.stem.wordnet import WordNetLemmatizer

targets = []
targetTotalMap = {}
targetUtteranceMap = {}

lmtzr = WordNetLemmatizer()



def loadTargets(path,file):
	
	reader = open(path + '/' + file)
	lines = reader.readlines()
	for line in lines:
		line = line.strip()
		ret = checkWNPresence(line)
		if ret == True:
			line = lmtzr.lemmatize(line)
		targets.append(line)
	
	reader.close()
	return Set(targets)

def checkWNPresence (feature):
	if not wn.synsets(feature):
		return False
	else:
		return True #English Word	

def loadTokenizedInputPath(path, targets):
	
	filters =[]# ['.aa.' ,'.ab.', '.ac.' , '.ad.' , '.ae.' , '.af.','.ag.' ,'.ah.', '.ai.']
	files = os.listdir(path)
	for file in files:
		found = False
		if 'lemma.emoji' in str(file) :
			for filter in filters:
				if filter in str(file):
					found = True
					break
			
			if found == False:	
				print 'operating on: ' + str(file)
				loadTokenizedInput(path,file,targets)

def loadTokenizedInput(path, file, targets):

	reader = codecs.open(path + '/' + file, "r", "utf-8")

	lines = reader.readlines()
	
	ids = ['388033195050303488', '388029986390958080']
	
#	targets = ['cute']
	for line in lines:
	#	id = line.split('\t')[0]
	#	utterance = line.split('\t')[1]
		if ids[0] not in line and ids[1] not in line:
			continue
		
		
		tokens = line.strip().split()
		
		for target in targets:
			if target in tokens:
				old = targetTotalMap.get(target)
				if old is None:
					old = 0
				targetTotalMap[target] = old + 1
				oldUtterances = targetUtteranceMap.get(target)
				if oldUtterances is None:
					oldUtterances = []
				oldUtterances.append(line)
				targetUtteranceMap[target] = oldUtterances

	reader.close()

	for target in targetTotalMap.keys():
		print target + '\t' + str(targetTotalMap.get(target))

def createAllTargetFiles(path):

	file = 'tweet.sarcasm.filtered.031012015.lemma.emoji.targets'
	file = 'aj.temp'
	writer = codecs.open(path + '/' + file + '.lemma.emoji', 'w', 'utf-8')

	for target in targetUtteranceMap.keys():
		utterances = targetUtteranceMap.get(target)
		
		for utterance in utterances:
			writer.write(target + '\t' + utterance)
			writer.write('\n')
	
	writer.close()
			

def main():
	
	mainPath = '/Users/dg513/work/eclipse-workspace/lucene-workspace/wordVectorLucene/'
	config = './data/config/'
	targetFile = 'sarcasm_target_shortlist.txt'
	#path = './data/input/sarcasm_tokens/'
	path = './data/input/random_tokens/'
	file = 'tweet.sarcasm.all.02192015.tokens.lemma.emoji'
#	file = 'tweet.positive.all.nostem.02192015.tokens.lemma.emoji'
	file = 'tweet.rnd.aj.filtered.031012015.lemma.emoji'

	targetSet = loadTargets( config,targetFile)
#	loadTokenizedInputPath(path,targetSet)
	loadTokenizedInput(path,file,targetSet)
	createAllTargetFiles(path)
main()



