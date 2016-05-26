alltestIds = []

def loadAllTrainData(path1):

	#this is for sarcastic tweets
	sarcFile = 'tweet.SARCASM.alltargets.TRAIN'
	nonsarcFile = 'tweet.NON_SARCASM.alltargets.TRAIN'
	sarcNonsarcFile = 'tweet.alltargets.TRAIN'
	#now write with label
	writer = open(path1 + '/' + sarcNonsarcFile, 'w')
	
	reader1 = open(path1 + '/' + sarcFile )
	
	print 'reading training file: ' + sarcFile

	lines = reader1.readlines()
	
	for line in lines:
		features = line.strip().split('\t')
		target = features[0]
		id = features[1]
		message = features[2]
		if id in alltestIds:
			continue
			
		writer.write('1' + '\t' + target + '\t' + id + '\t' + message)
		writer.write('\n')
		
	reader1.close()
	
	reader2 = open(path1 + '/' + nonsarcFile )
	
	print 'reading training file: ' + nonsarcFile

	lines = reader2.readlines()
	
	for line in lines:
		features = line.strip().split('\t')
		target = features[0]
		id = features[1]
		message = features[2]
		if id in alltestIds:
			continue
	
		writer.write('0' + '\t' + target + '\t' + id + '\t' + message)
		writer.write('\n')
		
	reader2.close()
	writer.close()
	
	#done with writing the output file 


def loadAllTestData(path1):

	#this is for sarcastic tweets
	sarcFile = 'tweet.SARCASM.alltargets.TEST'
	nonsarcFile = 'tweet.NON_SARCASM.alltargets.TEST'
	sarcNonsarcFile = 'tweet.alltargets.TEST'
	#now write with label
	writer = open(path1 + '/' + sarcNonsarcFile, 'w')
	
	global alltestIds 
	alltestIds = []
	
	reader1 = open(path1 + '/' + sarcFile )
	
	print 'reading test file: ' + sarcFile
	
	
	lines = reader1.readlines()
	
	for line in lines:
		features = line.strip().split('\t')
		target = features[0]
		id = features[1]
		message = features[2]
		alltestIds.append(id)
		writer.write('1' + '\t' + target + '\t' + id + '\t' + message)
		writer.write('\n')
		
	reader1.close()
	
	reader2 = open(path1 + '/' + nonsarcFile )
	
	print 'reading test file: ' + nonsarcFile

	lines = reader2.readlines()
	
	for line in lines:
		features = line.strip().split('\t')
		target = features[0]
		id = features[1]
		message = features[2]
		alltestIds.append(id)
		writer.write('0' + '\t' + target + '\t' + id + '\t' + message)
		writer.write('\n')
		
	reader2.close()
	writer.close()
	
	#done with writing the output file 
	
def main():
	path = './data/input/'
	loadAllTestData(path)
	loadAllTrainData(path)
	
main()