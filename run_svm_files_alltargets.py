import ConfigParser
import sys, getopt
import os
import codecs
import datetime

configPath = './data/config'

def joinFiles(target, suffix, input,output,mode):
	
	allData = []
	if suffix == 'TRAIN':
		path  = input + '/sarcasm_training/'
	if suffix == 'TEST':
		path  = input + '/sarcasm_testing/'
		
	fileName ='tweet' + '.'+ 'SARCASM' + '.' + target + '.' + suffix
	print 'file reading: ' + fileName
	reader = codecs.open(path + str(fileName),'r','utf-8')
	
	lines = reader.readlines()
	for line in lines:
		features = line.strip().split('\t')
		tweet = features[2]
		allData.append('1' + ' ' + tweet)
		
	reader.close()
	
	if suffix == 'TRAIN':
		if mode == 'LITERAL':
			path  = input + '/random_training/'
		if mode == 'LITERAL_SENT':
			path = input + '/positive_training/'
		else:
			print 'select proper mode for running experiments'

	if suffix == 'TEST':
		if mode == 'LITERAL':
			path  = input + '/random_testing/'
		if mode == 'LITERAL_SENT':
			path = input + '/positive_testing/'
		else:
			print 'select proper mode for running experiments'
	
	
	fileName ='tweet' + '.'+ 'NON_SARCASM' + '.' + target + '.' + suffix
	print 'file reading: ' + fileName
	reader = codecs.open(path + str(fileName),'r','utf-8')
	
	lines = reader.readlines()
	for line in lines:
		features = line.strip().split('\t')
		tweet = features[2]
		allData.append('0' + ' ' + tweet)

	return allData

def loadTargetNames(path,file):

    reader = open(path + '/'+file)
    lines =reader.readlines()
    global targetList
    targetList = [line.strip() for line in lines]

def config(configFile):
	config = ConfigParser.ConfigParser()
	config.read(configPath + '/' + configFile)
	global targetFile
	targetFile = config.get('section1','targetFile')
	global mode
	mode = config.get('section1', 'mode')
	global inputTrainPath
	inputTrainPath = config.get('section1', 'inputTrainPath')
	global inputTestPath
	inputTestPath = config.get('section1', 'inputTestPath')
	global trainPath
	trainPath = config.get('section1', 'trainPath')
	global testPath
	testPath = config.get('section1', 'testPath')
	global mainPath
	mainPath = config.get('section1', 'mainPath')
	global modelPath
	modelPath = config.get('section1','modelPath')
def main(argv):

	num = len(argv)
	if num < 2:
		print 'please pass config parameters'
		exit(1)
	conf = argv[0]
	if conf != '-c' :
		print 'check config parameters'
		exit(1)
	configFile = argv[1]
	config(configFile)
	#load the targets
	loadTargetNames(configPath,targetFile)
	
	#create the train test files for libSVM

	outputFile1 = 'SARC_NONSARC.'+'all' + '.TRAIN' 
	writer1 = codecs.open(trainPath + str(outputFile1),'w','utf-8')
	
	outputFile2 = 'SARC_NONSARC.'+'all'  + '.TEST' 
	writer2 = codecs.open(testPath + str(outputFile2),'w','utf-8')
	for target in targetList:
	#	print target
		trainData = joinFiles(target,'TRAIN',inputTrainPath,trainPath,mode)
		testData = joinFiles(target,'TEST',inputTestPath,testPath,mode)
	
		for t in trainData:
			writer1.write(t)
			writer1.write('\n')
		for t in testData:
			writer2.write(t)
			writer2.write('\n')

	writer1.close()
	writer2.close()

	target = 'ALL_TARGETS'
	
	java_train_command = '/usr/bin/java -Xmx12G -cp ' + mainPath+'libsvm_modified_0513.jar com.wsd.kernel.svm_train '
	train_command = '-a ' + target + ' ' + trainPath+outputFile1 + ' ' + modelPath + target + '.model' + ' '  
	train_command = java_train_command + train_command
	print 'train command: ' + train_command
	os.system(train_command)
		
	java_test_command = '/usr/bin/java -Xmx12G -cp ' + mainPath+'libsvm_modified_0513.jar com.wsd.kernel.svm_predict '
	test_command = '-a ' + target + ' ' + testPath+outputFile2 + ' ' + modelPath + target + '.model' + ' ' + modelPath + target + '.op'  
	test_command = java_test_command + test_command
	print 'test command: ' + test_command
	os.system(test_command)
		
	st = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
	print 'target finished: ' +target + ' '+ st
	print '\n'
	
if __name__ == "__main__":
   main(sys.argv[1:])
