import os
import codecs

import nltk
from nltk.corpus import wordnet as wn

from nltk.tokenize import WordPunctTokenizer
from nltk.stem.wordnet import WordNetLemmatizer
lmtzr = WordNetLemmatizer()


#this program will collect the test data (i.e. tweets) and prepare the list of gloss definitions
#from the text as well as the neighbours 
#we will store them separately for late use
stopList = []
targetList = []

def loadStopWords(path,file):
	reader = open(path + '/' + file)
	global stopList
	stopList = [ line.strip() for line in reader.readlines() ]


def loadTestFolderForGlossCollection(output,input):
	
	files = os.listdir(input)
	
	for file in files:
		if 'Store' in str(file) :
			continue
		
		reader = codecs.open(input + str(file),'r','utf-8')
		
		if not 'always' in str(file):
			continue 
		
	#	writer = codecs.open(output + '/' + file + '.glossdefn', 'w', 'utf-8')
		
		print 'file operating...' + ' ' + str(file)
		
		lines = reader.readlines()
		
		for line in lines:
			features = line.strip().split('\t')
			target = features[0]
			id = features[1]
			testData = features[2]
			featuresPOS = nltk.pos_tag(WordPunctTokenizer().tokenize(testData)) #because it is unicode

			if id != '502907101375913986':
				continue 


			for featPos in featuresPOS:
				feature = featPos[0]
				pos = featPos[1]
				feature = feature.lower()
				if feature == target:
					continue 
				if feature in stopList:
					continue
			#otherwise check for gloss	
			#first check if the word is in WordNet
				presence = checkWNPresence(feature)
				if presence == True:
					glossDefinitions = getGlossDefinitions(feature,pos)
					ret = target +'\t' + id + '\t' + testData.strip()  + '\t' + feature +':' + '\t' + format(glossDefinitions)
			#		print ret.encode('utf-8').strip()
	#				writer.write(ret)
	#				writer.write('\n')
		
	#	writer.close()
		reader.close()
		
		
def loadTestFileForGlossCollection(path, file):
	
	reader = open(path + '/' + file)
	lines = reader.readlines()
	
	writer = open ( path + '/' + file + '.glossdefn', 'w')
	
	index = 0 
	for line in lines:
		index = index + 1
		features = line.strip().split('\t')
		id = features[0]
		testData = features[1]
		featuresPOS = nltk.pos_tag(WordPunctTokenizer().tokenize(testData)) #because it is unicode

		target = findTarget(features)
		if target is None:
			print 'no target: ' + line
			continue 
		
		for feature in featuresPOS:
			feature = feature.lower()
			if feature == target:
				continue 
			if feature in stopList:
				continue
			#otherwise check for gloss	
			#first check if the word is in WordNet
			presence = checkWNPresence(feature)
			if presence == True:
				glossDefinitions = getGlossDefinitions(feature)
				if glossDefinitions is None:
					glossDefinitions = []
				ret = str(index) + '\t' + id + '\t' + testData.strip() + '\t' + target + '\t' + feature +':' + '\t' + format(glossDefinitions)
				print ret
				writer.write(ret)
				writer.write('\n')
		
		
				
	writer.close()
	reader.close()
	
def loadSelectedTargets(path,file):	
	global targetList 
	reader = open(path + file)
	lines = reader.readlines()
	targetList = [line.strip() for line in lines]

def loadTargets(path, file):

#we will improve this stage with MT translation but for the time being
#load the targets learned from the paraphrases
	global targetList 
	reader = open(path + file)
	lines = reader.readlines()
	for line in lines:
		line = line.strip()
		features = line.split('<->')
		first = features[0].strip().lower()
		second = features[1].strip().lower()
		if len(first) > 2:
			targetList.append(first)
		if len(second) > 2:
			targetList.append(second)
	
	reader.close()
	
	
	for target in targetList:
		print target 
	
	

def findTarget ( features ):
	
	#for the time being - use a list of tokens that represent sarcasm/non-sarcasm in the sentiment space
	#targets = ['love','great','amazing','awesome','like','great', 'funny','uplifting', 'loving','positive']
	
	for feature in features:
		feature = feature.lower()
		if feature in targetList:
			return feature
	
	return None

def checkWNPresence (feature):
	if not wn.synsets(feature):
		return False
	else:
		return True #English Word	
	
def getGlossDefinitions ( feature, featPos ):
	
	glossMap = {}
	#the feature is present 
	#feature = 'bar'
	if featPos.startswith('V'):
		featPos = 'v'
	elif featPos.startswith('N'):
		featPos = 'n'
	elif featPos.startswith('J'):
		featPos = 'a'
	elif featPos.startswith('RB'):
		featPos = 'r'
	else:
		return glossMap
	
#	synsets = wn.synsets(feature,pos=featPos)
	synsets = wn.synsets(feature)

	
#	dog_synset_1 = wn.synset('dog.n.01')
#	print dog_synset_1.definition
	
	#result = (wn.synset('dog.n.01').definition())
	 
	for syns in synsets:
		pos = syns.pos
		if pos != featPos:
			continue
		definition = syns.definition
		definition = lemmatize(definition)
		tuple = 'original'+'_'+str(syns),str(definition)
		tuples = glossMap.get(str(syns))
		if tuples is None:
			tuples = []
		
		tuples.append(tuple)
	 	glossMap[str(syns)] = tuples
	 
	 	hypernyms = syns.hypernyms()
	 	for hyper in hypernyms:
	 		definition = hyper.definition
	 		definition = lemmatize(definition)
	 		tuple = 'hypernym'+'_'+str(hyper), str(definition)
	 		tuples = glossMap.get(str(syns))
	 		if tuples is None:
			 	tuples = []
		
		 	tuples.append(tuple)
	 		glossMap[str(syns)] = tuples
	 	
	 	
	 	hyponyms = syns.hyponyms()
	 	for hypo in hyponyms:
	 		definition = hypo.definition
	 		definition = lemmatize(definition)
	 		tuple = 'hyponym'+'_'+str(hypo), str(definition)
	 		tuples = glossMap.get(str(syns))
	 		if tuples is None:
			 	tuples = []
		
		 	tuples.append(tuple)
	 		glossMap[str(syns)] = tuples
	 		
	 	meronym = syns.part_meronyms()
	 	for mero in meronym:
	 		definition = mero.definition
	 		definition = lemmatize(definition)
	 		tuple = 'meronym'+'_'+str(mero), str(definition)
	 		tuples = glossMap.get(str(syns))
	 		if tuples is None:
			 	tuples = []
		
		 	tuples.append(tuple)
	 		glossMap[str(syns)] = tuples
	 		
	 	
	 	holonym = syns.member_holonyms()
	 	for holo in holonym:
	 		definition = holo.definition
	 		definition = lemmatize(definition)
	 		tuple = 'holonym'+'_'+str(holo), str(definition)
	 		tuples = glossMap.get(str(syns))
	 		if tuples is None:
			 	tuples = []
		
		 	tuples.append(tuple)
	 		glossMap[str(syns)] = tuples
	 
	 #		glossMap['hypernym' + '_' + str(syns) + '_' + str(hyper)] = str(definition)
	 
	#now collect the hypernym, meronym, similar attributes etc.
	return glossMap

def lemmatize(defn):

	words = nltk.word_tokenize(defn)
	newwords = [ lmtzr.lemmatize(word) for word in words]
	return ' '.join(newwords)

def format(glossMap):
	
	ret = '\t'
	for key in glossMap.keys():
		ret = ret + key + '|||'
		values = glossMap.get(key)
		for value in values:
			ret = ret  + '|||' + str(value) 
		ret = ret + '\t'	
	ret = ret.strip()
	return ret


def main():
	
	configPath = './data/config/'
	stopFile = 'stopwords_small.txt'
	targetParaphraseFile = 'sarcasm_target_shortlist.txt'
	targetParaphraseFile = 'topnames.txt'
	
	wsdPath = './data/output/WSDFolder/sarcasm_testing_wsd/'
#	wsdPath = './data/output/WSDFolder/random_testing_wsd/'

#	sarcTestFile = 'rawcorpus_s_ns.txt'
#	sarcTestFile = 'HitList_1000_id_sarc.txt'
	testFolder = './data/input/sarcasm_testing/'
#	testFolder = './data/input/random_testing/'

	#sarc
	
	#we need to lemmatize the text
	
	loadStopWords(configPath,stopFile)
	loadSelectedTargets(configPath,targetParaphraseFile)
#	loadTargets(configPath,targetParaphraseFile)
#	loadTestFileForGlossCollection(testPath,sarcTestFile)
	loadTestFolderForGlossCollection(wsdPath,testFolder)
	
	
main() 