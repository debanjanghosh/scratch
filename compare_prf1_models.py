import math


total_f1 = 0
total_p = 0
total_r = 0
max_f1 = 0 
min_f1 = 10000
max_p = 0
min_p = 10000
max_r = 0
min_r = 10000
max_cat = ''
min_cat = ''
p_all = []
r_all = []
f1_all = []

def readOutputWeiwei(output2,outputs1,input,op2,ops1,ip,target,writer):
    
     
    f1 = open(output2 + op2)
    lines = f1.readlines()
    preds2 = [ line.split()[0] for line in lines]
    f1.close()
    
    preds1 = [0 for index in range(len(lines))] 
    # we keep only one copy of preds1
    # that is the best results!
    for index1 in range(len(outputs1)):
        op1 = ops1[index1]
        output1 = outputs1[index1]
        f1 = open(output1 + op1)
        lines = f1.readlines()
        for index2 in range(len(lines)):
            line = lines[index2]
            op = line.split()[0]
            if op == '1': # we care only about sarc examples
                preds1[index2] = op

        f1.close()
    
    f1 = open(input +ip)
    lines = f1.readlines()
    golds = [ line.split()[0] for line in lines]
    gold_text = [ line.split('\t')[3].strip() for line in lines]

    f1.close()

    eval_accuracy3(preds2,preds1,golds,gold_text,target,writer)
    
def eval_accuracy3(preds2,preds1,golds,gold_text,target,writer):
    
    pos= 1
    pos_correct2 = []
    pos_correct1 = []

    index = -1
    for p,g in zip(preds2,golds):
        index=index+1
        #if int(g) == pos and int(p) == pos :
        if int(g) == pos and float(p) == pos :
            pos_correct2.append(index)
    
    index = -1
    for p,g in zip(preds1,golds):
        index=index+1
        #if int(g) == pos and int(p) == pos :
        if int(g) == pos and float(p) == pos :
            pos_correct1.append(index)
    
    for posn in pos_correct2:
        if posn not in pos_correct1:
            print 'target:' + '\t' + target + '\t' + 'line number:' + '\t' + str(posn) + '\t' + gold_text[posn]
            writer.write('target:' + '\t' + target + '\t' + 'line number:' + '\t' + str(posn) + '\t' + gold_text[posn])
            writer.write('\n')


def eval_accuracy( preds, golds):
        fine = sum([ sum(int(p) == int(y)) for p,y in zip(preds, golds) ]) + 0.0
        fine_tot = sum( [ len(y) for y in golds ] )
        pos_total = 0
        pos_correct = 0
        pos_pred_total = 0
        pos = '1'
        for ps, ys in zip(preds, golds):
             for p, y in zip(ps, ys):
                if y == self.vocaby[pos]:
                    pos_total += 1
                if p == self.vocaby[pos]:
                    pos_pred_total += 1
                if y == self.vocaby[pos] and p == self.vocaby[pos]:
                    pos_correct += 1
        print 'Pos total: ' + str(pos_total), 'Pos correct: ' + str(pos_correct),  'Pos predicted: ' + str(pos_pred_total)
        precision = pos_correct/float(pos_pred_total) if (pos_pred_total > 0) else 0
        recall = pos_correct/float(pos_total)
        f_score = 2 * precision * recall / (precision + recall) if (precision > 0 and recall > 0) else 0
        return fine/fine_tot, precision, recall, f_score
    

def loadTargets():
   # path = '../../vector/data/config/'
    path = './data/config/'
    file = 'targets.txt'
    f = open(path + file)
    targets = [line.strip() for line in f.readlines() ]
    return targets

if __name__=="__main__":
  
    output2 = './data/output/compare/preds_single_ts/'
 #   output2 = '/Users/dg513/work/eclipse-workspace/sarcasm-workspace/SarcasmDetection/data/twitter_corpus/models/samelm2/'
    
    outputs1 = []
    outputs1.append('./data/output/compare/preds_single/')
    outputs1.append('./data/output/compare/preds_multi/')
    outputs1.append('./data/output/compare/preds_svmwe/')

    
    input  = '/Users/dg513/work/eclipse-workspace/sarcasm-workspace/SarcasmDetection/data/twitter_corpus/wsd/sentiment/samelm2/weiwei/'

    
  #  input = './data/output/samelm/ids/senti/2_cnn/'


    targets = loadTargets()
    total_f1 = 0 
  #  targets = ['always', 'amazing','better', 'best']
  #  targets = ['attractive']
  #  targets = ['attractive','amazing','better', 'best']
    

    #targets = ['always', 'amazing','better', 'best','beautiful','wonder','super','fantastic']
    writer = open('./data/output/compare/cnn_ts_vs_allem.txt','w')

    for target in targets:
        files1 = []

        op2 = target + '.ts.pred' #.0222.pred'

     #   op = 'output_predict_samelm_' + target + '.cbow.txt'
        op1 = target + '.0222.pred'
        files1.append(op1)
        op1 = target + '.0222.pred'
        files1.append(op1)
        op1 = target + '.op' #.0222.pred'
        files1.append(op1)


        ip = 'tweet.' + target + '.target.TEST'
    #    ip = 'tweet.' + target + '.target.SENTI.BOTH.TEST' + '.id'

        readOutputWeiwei(output2,outputs1,input,op2,files1,ip,target,writer)

    writer.close()