#online generation of BoW inner product for gloss_definition (context) vs. WS (targets)

import os

unigrams = {}

def loadGlossDefinitionFile(path,file):

	reader = open(path + '/' + file)
	lines = reader.readlines()
	
	for line in lines:
		
	#	print line
	#	if line.startswith('306'):
	#		print 'original msg' + ' ' + line.strip()
		
		features = line.split('\t')
		index = features[0]
		utterance = features[1]
		target = features[2]
		targetVector = getTargetVector(target)
		contextWord = features[3][0:len(features[3])-1]
		
		for index in range (4,len(features)): #why 5? extra tab?
			glossDefn = features[index] 
			#first get the synset
			##Synset('yes.n.01'):
			origSynset = glossDefn.split('||||||')[0]
			#:[("original_Synset('yes.n.01')", 'an affirmative'), ("hypernym_Synset('affirmative.n.01')", 'a reply of affirmation')]	
			allDefn = glossDefn.split('||||||')[1]
			#remove the [ and ]
		#	allDefn = allDefn[1:len(allDefn)-1]
			elements = allDefn.split('|||')
			for element in elements:
				#("original_Synset('yes.n.01')", 'an affirmative'),
				element = element.strip()
				if not element:
					continue 
				element = element[1:len(element)-1]
				elem_key = element.split(',')[0]
				elem_value  = element.split(',')[1].strip()
				elem_value = elem_value[1:len(elem_value)-1]
				elemVector = getElemVector(elem_value)
				#print str(elem_value.strip())
				dot = innerproduct(targetVector,elemVector)
	reader.close()

def innerproduct(tVector,cVector):
	
	return 1.0

def loadVocabulary ( path, file):

	reader = open(path + '/' + file)
	lines = reader.readlines()
	global unigrams 
	
	for line in lines:
		features = line.split('\t')
		unigrams[features[0]] = features[1]
	
	reader.close()

def loadAllTargetVectors(path):

	fileList = os.listdir(path)
	
	for file in fileList:
		reader = open(path + '/' + file)
		lines = reader.readlines()


def main():
	
	testPath = './data/output/WSDFolder/'
	sarcTestFile = 'rawcorpus_s_ns.txt.glossdefn'
	loadGlossDefinitionFile(testPath,sarcTestFile)
	
main() 