#lemmatization function using WordNet
#little silly to use it though - can use StanfordParser too

import codecs
from sets import Set

from nltk.corpus import wordnet as wn

from nltk.stem.wordnet import WordNetLemmatizer

import re
import urllib2

lmtzr = WordNetLemmatizer()

def createInputFileForSelectiveTargets(path,file):
	
	hashtags = ["#sarcasm", "#sarcastic" , "#angry" , "#awful" , "#disappointed" ,
			  "#excited", "#fear" ,"#frustrated", "#grateful", "#happy" ,"#hate",
			  "#joy" , "#loved", "#love", "#lucky", "#sad", "#scared", "#stressed",
			  "#wonderful", "#positive", "#positivity", "#disappointed", "#irony" ]
	
	reader = codecs.open(path + '/' + file, "r", "utf-8")
	#reader = open(path + '/' + file)
	#writer = open(path + '/' + file + '.lemma.temp', 'w')
	writer = codecs.open(path + '/' + file + '.lemma.emoji', 'w', 'utf-8')
	
	lines = reader.readlines()
	lineNum = 0
	for line in lines:
		features = line.split('\t')
		id = features[0]
		text = features[1]
		tokens = text.split()
		
	#	if id != '544237715370573825':
	#		continue 
		
		token_lemma = ''
		for token in tokens:
			if token in hashtags:
				continue
			type = checkUnicode(token)
			if type == 1:
				token_list = list(token.decode("utf-8"))
				token_pair_list = [token_list[index*2] + token_list[index*2+1] for index in range(0,len(token_list)/2)]
				token_pair_set = Set(token_pair_list)
				token = ' '.join(token_pair_set)
			elif type ==2:
				continue
			else:	
				ret = checkWNPresence(token)
				if ret == True:
					token = lmtzr.lemmatize(token)
			token_lemma = token_lemma + ' ' + token
		
		token_lemma = token_lemma.strip()
		writer.write('shocked' + '\t' + id + '\t' + token_lemma)
		writer.write('\n')		
		lineNum = lineNum + 1
	#	print 'number of line done: ' + str(lineNum) 
		if lineNum%100 == 0:
			print 'number of line done: ' + str(lineNum) 
	reader.close()
	writer.close()		


def loadTestFile(path,file):
	
	reader = codecs.open(path + '/' + file, "r", "utf-8")
	#reader = open(path + '/' + file)
	#writer = open(path + '/' + file + '.lemma.temp', 'w')
	writer = codecs.open(path + '/' + file + '.lemma.emoji', 'w', 'utf-8')
	
	lines = reader.readlines()
	lineNum = 0
	for line in lines:
		features = line.split('\t')
		id = features[1]
		text = features[5]
		tokens = text.split()
		
	#	if id != '544237715370573825':
	#		continue 
		
		token_lemma = ''
		for token in tokens:
			type = checkUnicode(token)
			if type == 1:
				token_list = list(token.decode("utf-8"))
				token_pair_list = [token_list[index*2] + token_list[index*2+1] for index in range(0,len(token_list)/2)]
				token_pair_set = Set(token_pair_list)
				token = ' '.join(token_pair_set)
			elif type ==2:
				continue
			else:	
				ret = checkWNPresence(token)
				if ret == True:
					token = lmtzr.lemmatize(token)
			token_lemma = token_lemma + ' ' + token
		
		token_lemma = token_lemma.strip()
		writer.write(id + '\t' + token_lemma)
		writer.write('\n')		
		lineNum = lineNum + 1
	#	print 'number of line done: ' + str(lineNum) 
		if lineNum%10000 == 0:
			print 'number of line done: ' + str(lineNum) 
	reader.close()
	writer.close()			

def checkUnicode (token):
	
	try:
		token.decode('ascii')
	except UnicodeDecodeError:
		return 1
	except UnicodeEncodeError:
		return 2
	else:
		return 0
	 
	
	
	#if isinstance(token, str):
#		token.decode('ascii')#
	#	return False
	#elif isinstance(token, unicode):
	#	return True
	#else:
	#	print "not a string/Unicode"
	#	return False
	
def checkWNPresence (feature):
	if not wn.synsets(feature):
		return False
	else:
		return True #English Word	
	
def main():
	
	mainPath = '/Users/dg513/work/eclipse-workspace/distrib-workspace/wordVectorLucene/'
	path = './data/input/positive_tokens/'
	#file = 'tweet.sarcasm.all.02192015.tokens'
#	file = 'tweet.positive.all.nostem.02192015.tokens'
	file = 'tweet.rnd.aj.filtered.031012015'
	file = 'tweet.allnegatives.201314'
	file = 'shocked.1214.txt'
#	file = 'text.wem.tokens'
	#checkEmoji(path,file)
	#loadTestFile( path,file)
	createInputFileForSelectiveTargets(path,file)
	
main()
