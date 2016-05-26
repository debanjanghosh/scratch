#create new training data for each target
#simply parse the file and create the training
import codecs
import random

targetTextMap = {}

def loadTargets(path,file):
	reader = open(path + file)
	lines = reader.readlines()
	global targets
	targets = [ line.strip() for line in lines]


def readParsedFile(path,training,testing,file,type):
	reader = open(path + file)
	lines = reader.readlines()
	
	total = len(lines)
	num = 0
	for line in lines:
		num = num +1
	#	if num == 10000:
	#		break
		features = line.split()
		for target in targets:
			if target in features:
				alllines = targetTextMap.get(target)
				if alllines is None:
					alllines = []
				alllines.append(line)
				targetTextMap[target] = alllines
				
				
				
	
	#now write 
	#for each target - create 
	#we can later combine targets like "love" and "loved"
	for target in targets:
		writer1 = codecs.open(training + file + '.' + target + '.'+'training', 'w', 'utf-8')
		writer2 = codecs.open(testing + file + '.' + target + '.' + 'testing', 'w', 'utf-8')

		if target == 'mature':
			print 'here'
		alllines = targetTextMap.get(target)
		#for testing - set the maximum # of test data to 100
		#first shuffle the list
		random.shuffle(alllines)
		size = len(alllines)
		size_10 = size/10
		size_remaining = size-size_10
		
		if  type == 'positive':
			#we need a balanced data set 
			#so check the line numbers for sarcasm
			train_test_num = getBalancedNumber(target)
		#	train_test_num = (5,5)
			train_num = train_test_num[0]
			test_num = train_test_num[1]
			if  size_10 < test_num and size_remaining < train_num : #really low!
				testlist = alllines[0:size_10]
				trainlist = alllines[size_10+1:]
			else:
				testlist = alllines[0:train_test_num[1]]
				trainlist = alllines[train_test_num[0]+1:]
			
		else:	
			if ( size_10 < 100):
				testlist = alllines[0:size_10]
				trainlist = alllines[size_10+1:]
			else:
				testlist = alllines[0:100]
				trainlist = alllines[101:]
			
		for line in trainlist:
			writer1.write(target + '\t' + line)
		writer1.close()
		
		for line in testlist:
			writer2.write(target + '\t' + line)
		writer2.close()
	
def getBalancedNumber(target):
	
	
	trainingPath = './data/input/sarcasm_training/'
	reader = open(trainingPath + 'tweet.sarcasm.all.02192015.tokens.lemma.emoji.' + target + '.'+'training')
	training_num = len(reader.readlines())
	
	testingPath = './data/input/sarcasm_testing/'
	reader = open(testingPath + 'tweet.sarcasm.all.02192015.tokens.lemma.emoji' + '.' + target + '.'+'testing')
	testing_num = len(reader.readlines())
	
	return (training_num,testing_num)
	
	
def main():
	
	config = './data/config/'
	#input = './data/input/sarcasm_tokens/'
	#training = './data/input/sarcasm_training/'
	#testing = './data/input/sarcasm_testing/'
	input = './data/input/positive_tokens/'
	training = './data/input/positive_training/'
	testing = './data/input/positive_testing/'

	
	#file = 'tweet.sarcasm.all.02192015.tokens'
	targetFile = 'sarcasm_target_shortlist.txt'
#	tokenFile = 'tweet.sarcasm.all.02192015.tokens.lemma.emoji'
	tokenFile = 'tweet.positive.all.nostem.02192015.tokens.lemma.emoji'
	
#	file = 'text.wem.tokens'
	#checkEmoji(path,file)
	type = 'positive'
	loadTargets( config,targetFile)
	readParsedFile(input,training,testing,tokenFile,type)
	
main()
		