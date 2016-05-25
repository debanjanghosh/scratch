import operator
from collections import defaultdict
import string
import numpy as np
import scipy.sparse
import sys, getopt

stopWords = open('./data/intern/stop-words.txt').read().split()

def getLines(file):
	lines = []
	with open(file) as raw:
		sents = raw.read().lower().splitlines()
	for sent in range(0, len(sents)):
		exclude = set(string.punctuation)
		sents[sent] = ''.join(ch for ch in sents[sent] if ch not in exclude)
		lines.append(sents[sent].split())
	return lines

# WORD = 'now'

def mima(MINORMAX, val, given):
	if MINORMAX is 'min':
		if given < val:
			given = val
	if MINORMAX is 'max':
		if given > val:
			given = val 
	return given

def getContext(word, data, windowSize):
	# data = getLines(data_raw)
	contexts = defaultdict(lambda: 0)
	for line in range(0, len(data)):
		if word in data[line]:
		#	print data[line]
			for i in range(mima('min', 0, data[line].index(word)-windowSize), mima('max', len(data[line]), data[line].index(word)+windowSize + 1)):
				# print data[line][i]
				if data[line][i] not in stopWords and data[line][i] != word:
					contexts[data[line][i]] += 1
	# return sorted(contexts.items(), key=operator.itemgetter(1), reverse=True)
	return contexts

# def wordCount(word, data)
# 	tmp = 0
# 	for line in range(0, len(data)):
# 		for i in range(0, len(data[line])):
# 			if data[line][i] is word:
# 				tmp += 1
# 	return tmp

def cosSim(data1, data2):
	all_keys=np.unique(np.array((data1,data2)).T[0])
	array1=np.array([[i,data1.get(i,0)] for i in all_keys])
	array2=np.array([[i,data2.get(i,0)] for i in all_keys])

	array1_i = np.array([i[1] for i in array1], dtype=float)
	array2_i = np.array([i[1] for i in array2], dtype=float)

	return (np.dot(array1_i, array2_i))/(np.linalg.norm(array1_i)*np.linalg.norm(array2_i))

def main(argv):

	CORPUS = getLines('./data/intern/corpus.txt')
	WINDOW_SIZE = 5

	word1 = "life"
	word2 = "death"

	data1 = getContext(word1, CORPUS, WINDOW_SIZE)
	data2 = getContext(word2, CORPUS, WINDOW_SIZE)
	
	print cosSim(data1, data2)

	# vect1 = arrayInit(data1)
	# vect2 = arrayInit(data2)
	# print array1, "\n", array2
	# print array1_i, "\n", array2_i
	# print np.dot(array1_i, array2_i)
	# print np.dot(vect1,vect2)
	# print data

	# print wordCount(WORD, CORPUS)
	# vect1 = np.array(getContext('hate', CORPUS, WINDOW_SIZE))
	# vect2 = np.array(getContext('love', CORPUS, WINDOW_SIZE))
	# print np.dot(vect1, vect2)

if __name__ == "__main__":
	main(sys.argv[1:])