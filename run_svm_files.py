import sys, getopt
import os
import codecs
import datetime

def joinFiles(target, suffix, input,output):
	
	if suffix == 'TRAIN':
		path  = input + '/sarcasm_training/'
	if suffix == 'TEST':
		path  = input + '/sarcasm_testing/'
		
	fileName ='tweet' + '.'+ 'SARCASM' + '.' + target + '.' + suffix
	print 'file reading: ' + fileName
	reader = codecs.open(path + str(fileName),'r','utf-8')
	outputFile = fileName+'all' +'.' + suffix 
	writer = codecs.open(output + str(outputFile),'w','utf-8')
	
	lines = reader.readlines()
	for line in lines:
		features = line.strip().split('\t')
		tweet = features[2]
		writer.write('1' + ' ' + tweet)
		writer.write('\n')
		
	reader.close()
	
	if suffix == 'TRAIN':
		path  = input + '/positive_training/'
	if suffix == 'TEST':
		path  = input + '/positive_testing/'
	
	
	fileName ='tweet' + '.'+ 'NON_SARCASM' + '.' + target + '.' + suffix
	print 'file reading: ' + fileName
	reader = codecs.open(path + str(fileName),'r','utf-8')
	
	lines = reader.readlines()
	for line in lines:
		features = line.strip().split('\t')
		tweet = features[2]
		writer.write('0' + ' ' + tweet)
		writer.write('\n')

	writer.close()
	return outputFile

def loadTargetNames(path,file):

    reader = open(path + '/'+file)
    lines =reader.readlines()
    global targetList
    targetList = [line.strip() for line in lines]

def main():

	#load the targets
	configPath =  './data/config/'
	targets = 'topnames_few.txt'
	loadTargetNames(configPath,targets)
	
	
	inputTrainPath = './data/input/' ;
	inputTestPath = './data/input/' ;
	
	
	trainPath = './data/input/train/' ;
	testPath = './data/input/test/' ;
	modelPath ='./data/model/' 
	#create the train test files for libSVM

	mainPath = '/Users/dg513/work/eclipse-workspace/lucene-workspace/WSDLibSVM/'
#	mainPath = '/scratch/dg513/work/sarcasm/vector/'
#	mainPath = '/export/projects/sarcasm/vector/'
	#prf1Scores = open(modelPath + 'prf1_scores.txt','w')
	for target in targetList:
		#if target.strip() !='attractive':
		#	continue 
	#	print target
		trainFile = joinFiles(target,'TRAIN',inputTrainPath,trainPath)
		testFile = joinFiles(target,'TEST',inputTestPath,testPath)
		java_train_command = '/usr/bin/java -Xmx4G -cp ' + mainPath+'libsvm_modified_0519_modthreshold.jar com.wsd.kernel.svm_train '
		train_command = '-a ' + target + ' ' + trainPath+trainFile + ' ' + modelPath + target + '.model' + ' '  
		train_command = java_train_command + train_command
		print 'train command: ' + train_command
		os.system(train_command)
		
		java_test_command = '/usr/bin/java -Xmx4G -cp ' + mainPath+'libsvm_modified_0519_modthreshold.jar com.wsd.kernel.svm_predict '
		test_command = '-a ' + target + ' ' + testPath+testFile + ' ' + modelPath + target + '.model' + ' ' + modelPath + target + '.op'  
		test_command = java_test_command + test_command
		print 'test command: ' + test_command
		os.system(test_command)
		
		st = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
		print 'target finished: ' +target + ' '+ st
		print '\n'
	
if __name__ == "__main__":
   main()
