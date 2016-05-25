import sys, getopt

def joinFiles(first, second, path):
	
	reader = open(path + '/' + first)
	writer = open(path + '/'+first + '.train', 'w')
	lines = reader.readlines()
	for line in lines:
		features = line.strip().split('\t')
		tweet = features[2]
		writer.write('1' + ' ' + tweet)
		writer.write('\n')
		
	reader.close()
	
	reader = open(path + '/' + second)
	lines = reader.readlines()
	for line in lines:
		features = line.strip().split('\t')
		tweet = features[2]
		writer.write('0' + ' ' + tweet)
		writer.write('\n')

	writer.close()

def main(args):

	joinFiles(args[0],args[1],args[2])
	
if __name__ == "__main__":
   main(sys.argv[1:])