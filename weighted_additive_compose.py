import time
import math
from composes.semantic_space.space import Space
from composes.transformation.scaling.ppmi_weighting import PpmiWeighting
from composes.transformation.dim_reduction.svd import Svd
from composes.transformation.feature_selection.top_feature_selection import TopFeatureSelection
from composes.similarity.cos import CosSimilarity
from composes.utils import io_utils
from composes.utils import log_utils
from composes.composition.weighted_additive import WeightedAdditive
from composes.composition.full_additive import FullAdditive
from composes.utils.gen_utils import assert_is_instance
from composes.utils.matrix_utils import resolve_type_conflict
from composes.matrix.dense_matrix import DenseMatrix
import numpy as np

import logging
from composes.utils import log_utils as log

logger = logging.getLogger(__name__)

mainPath = '/Users/dg513/work/eclipse-workspace/compose-workspace/dissect/demo/'
mainPath = './data/dissect/training/'


MAX_MEM_OVERHEAD = 0.2
composed_id2column = None


alphaParam = 1.0
betaParam = 1.0 





def trainingWeightedAverageModel():
    
    core_cooccurrence_file  = mainPath + 'noun_adj_nytimes_071415.lower.sm'
    core_row_file = mainPath + 'noun_adj_nytimes_071415.lower.rows'
    core_col_file = mainPath + 'noun_adj_nytimes_071415.lower.cols'
  #  core_space_file = mainPath + 'core.pkl'
 
    per_cooccurrence_file  = mainPath + 'beautiful-phrase_pairs.sm'
    per_row_file = mainPath + 'beautiful-phrase_pairs.rows'
    per_col_file = mainPath + 'beautiful-phrase_pairs.cols'
    
 #   per_space_file = mainPath + 'sv.pkl'
  #  core_space_file = mainPath + "core.pkl"

    training_pair_file = mainPath + 'beautiful-training_pairs.txt'
    testing_pair_file = mainPath + 'beautiful-testing_pairs.txt'
    
    '''
    core_cooccurrence_file  = mainPath + 'core.sm'
    core_row_file = mainPath + 'core.rows'
    core_col_file = mainPath + 'core.cols'
    core_space_file = mainPath + 'core.pkl'
 
    per_cooccurrence_file  = mainPath + 'sv.sm'
    per_row_file = mainPath + 'sv.rows'
    per_col_file = mainPath + 'sv.cols'
    
    per_space_file = mainPath + 'sv.pkl'
    core_space_file = mainPath + "core.pkl"

    training_pair_file = mainPath + "training_pairs.txt"
    testing_pair_file = mainPath + "testing_pairs.txt"
    composed_space_file = mainPath + "composed.pkl"
    
    # config log file
   # log_utils.config_logging(log_file)
    '''
    print "Reading in train data"
    train_data = io_utils.read_tuple_list(training_pair_file, fields=[0,1,2])
    
    print "Training weighted average Function compositional model"
 #   core_space = io_utils.load(core_space_file)
 #   per_space = io_utils.load(sv_space_file)
    
    core_space = Space.build(data=core_cooccurrence_file, rows=core_row_file,
                                 cols=core_col_file, format="sm")
  
    per_space = Space.build(data=per_cooccurrence_file, rows=per_row_file,
                                 cols=per_col_file, format="sm")
    
 #   per_space = PeripheralSpace.build(core_space,data=per_cooccurrence_file, cols=per_col_file,
  #                                    rows=per_row_file, format="sm")
  
    
    comp_model = WeightedAdditive()
   # comp_model.train(train_data, core_space, per_space)
    
    print "Finding neighbors of \"conflict-n_erupt-v\" in the composed space"
   # composed_space = comp_model.compose([("erupt-v", "conflict-n","conflict-n_erupt-v")], core_space)
    test_phrases = io_utils.read_tuple_list(testing_pair_file, fields=[0,1,2])
    composed_space = comp_model.compose(test_phrases, core_space)
    neighbors = composed_space.get_neighbours('beautiful-jj_actress-nn', 10, CosSimilarity())
    print neighbors
    
'''
    print "Composing phrases"
    test_phrases = io_utils.read_tuple_list(testing_pair_file, fields=[0,1,2])
    composed_space = comp_model.compose(test_phrases, core_space)
    
    print "Saving composed space"
    io_utils.save(composed_space, composed_space_file)
'''
    
    
def trainingWeightedAverageModelHere():
   
    core_cooccurrence_file  = mainPath + 'noun_adj_nytimes_071415.lower.sm'
    core_row_file = mainPath + 'noun_adj_nytimes_071415.lower.rows'
    core_col_file = mainPath + 'noun_adj_nytimes_071415.lower.cols'
  #  core_space_file = mainPath + 'core.pkl'
 
    per_cooccurrence_file  = mainPath + 'beautiful-phrase_pairs.sm'
    per_row_file = mainPath + 'beautiful-phrase_pairs.rows'
    per_col_file = mainPath + 'beautiful-phrase_pairs.cols'
    
 #   per_space_file = mainPath + 'sv.pkl'
  #  core_space_file = mainPath + "core.pkl"

    training_pair_file = mainPath + 'beautiful-training_pairs.txt'
    testing_pair_file = mainPath + 'beautiful-testing_pairs.txt'
    
    '''core_cooccurrence_file  = mainPath + 'core.sm'
    core_row_file = mainPath + 'core.rows'
    core_col_file = mainPath + 'core.cols'
    core_space_file = mainPath + 'core.pkl'
 
    per_cooccurrence_file  = mainPath + 'sv.sm'
    per_row_file = mainPath + 'sv.rows'
    per_col_file = mainPath + 'sv.cols'
    
    per_space_file = mainPath + 'sv.pkl'
    core_space_file = mainPath + "core.pkl"

    training_pair_file = mainPath + "training_pairs.txt"
    testing_pair_file = mainPath + "testing_pairs.txt"
    composed_space_file = mainPath + "composed.pkl"
    '''
     # config log file
   # log_utils.config_logging(log_file)
    
    print "Reading in train data"
    train_data = io_utils.read_tuple_list(training_pair_file, fields=[0,1,2])
    
    print "Training weighted average Function compositional model"
 #   core_space = io_utils.load(core_space_file)
 #   per_space = io_utils.load(sv_space_file)
    
    core_space = Space.build(data=core_cooccurrence_file, rows=core_row_file,
                                 cols=core_col_file, format="sm")
  
    per_space = Space.build(data=per_cooccurrence_file, rows=per_row_file,
                                 cols=per_col_file, format="sm")
    
 #   per_space = PeripheralSpace.build(core_space,data=per_cooccurrence_file, cols=per_col_file,
  #                                    rows=per_row_file, format="sm")
  
    
    comp_model = WeightedAdditive()
    train(train_data, core_space, per_space)
    
    print "Finding neighbors of \"conflict-n_erupt-v\" in the composed space"
   # composed_space = comp_model.compose([("erupt-v", "conflict-n","conflict-n_erupt-v")], core_space)
    test_phrases = io_utils.read_tuple_list(testing_pair_file, fields=[0,1,2])
    composed_space = compose(test_phrases, core_space)
    print 'composition is done with test data'
    neighbors = composed_space.get_neighbours("beautiful-jj_actress-nn", 10, CosSimilarity())
    print neighbors
    
'''
    print "Composing phrases"
    test_phrases = io_utils.read_tuple_list(testing_pair_file, fields=[0,1,2])
    composed_space = comp_model.compose(test_phrases, core_space)
    
    print "Saving composed space"
    io_utils.save(composed_space, composed_space_file)
''' 
def compose( data, arg_space):
        """
        Uses a composition model to compose elements.
        Args:
            data: data to be composed. List of tuples, each containing 3
            strings: (arg1, arg2, composed_phrase). arg1 and arg2 are the
            elements to be composed and composed_phrase is the string associated
            to their composition.
            arg_space: argument space(s). Space object or a tuple of two
            Space objects (e.g. my_space, or (my_space1, my_space2)).
            If two spaces are provided, arg1 elements of data are
            interpreted in space1, and arg2 in space2.
        Returns:
            composed space: a new object of type Space, containing the
            phrases obtained through composition.
        """
        start = time.time()
        arg1_space, arg2_space = extract_arg_spaces(arg_space)
        arg1_list, arg2_list, phrase_list = valid_data_to_lists(data,
                                                                     (arg1_space.row2id,
                                                                      arg2_space.row2id,
                                                                      None))
        
        # we try to achieve at most MAX_MEM_OVERHEAD*phrase_space memory overhead
        # the /3.0 is needed
        # because the composing data needs 3 * len(train_data) memory (arg1 vector, arg2 vector, phrase vector)
        chunk_size = int(max(arg1_space.cooccurrence_matrix.shape[0],arg2_space.cooccurrence_matrix.shape[0],len(phrase_list))
                          * MAX_MEM_OVERHEAD / 3.0) + 1
        
        composed_mats = []
        for i in range(int(math.ceil(len(arg1_list) / float(chunk_size)))):
            beg, end = i*chunk_size, min((i+1)*chunk_size, len(arg1_list))

            arg1_mat = arg1_space.get_rows(arg1_list[beg:end])
            arg2_mat = arg2_space.get_rows(arg2_list[beg:end])

            [arg1_mat, arg2_mat] = resolve_type_conflict([arg1_mat, arg2_mat],
                                                                    DenseMatrix)
            composed_mat = composeFunction(arg1_mat, arg2_mat)
            composed_mats.append(composed_mat)
        
        composed_phrase_mat = composed_mat.nary_vstack(composed_mats)
        
   #     if composed_id2column is None:
        composed_id2column = build_id2column(arg1_space, arg2_space)

        print("\nComposed with composition model:")
        log.print_info(logger, 3, "Composed total data points:%s" % arg1_mat.shape[0])
        log.print_matrix_info(logger, composed_phrase_mat, 4,
                              "Resulted (composed) semantic space::")
        log.print_time_info(logger, time.time(), start, 2)
        
        return Space(composed_phrase_mat, phrase_list, composed_id2column)


def composeFunction(arg1_mat, arg2_mat):
    return alphaParam * arg1_mat + betaParam * arg2_mat    

def build_id2column( arg1_space, arg2_space):
        return arg1_space.id2column

def train(train_data, arg_space, phrase_space):
    
    start = time.time()

    arg1_space, arg2_space = extract_arg_spaces(arg_space)
    ids1 =arg1_space.row2id
    ids2 =arg2_space.row2id
    
    arg1_list, arg2_list, phrase_list = valid_data_to_lists(train_data,
                                                                     (arg1_space.row2id,
                                                                      arg2_space.row2id,
                                                                      phrase_space.row2id)
                                                                     )

    weightedAverageTrain(arg1_space, arg2_space, phrase_space, arg1_list, arg2_list, phrase_list)
    
    composed_id2column = phrase_space.id2column

    print 'trained composition model'
 #   log.print_composition_model_info(logger, 1, "\nTrained composition model:")
    log.print_info(logger, 2, "With total data points:%s" % len(arg1_list))
    log.print_matrix_info(logger, arg1_space.cooccurrence_matrix, 3,
                              "Semantic space of argument 1:")
    log.print_matrix_info(logger, arg2_space.cooccurrence_matrix, 3,
                              "Semantic space of argument 2:")
    log.print_matrix_info(logger, phrase_space.cooccurrence_matrix, 3,
                              "Semantic space of phrases:")
    log.print_time_info(logger, time.time(), start, 2)

def extract_arg_spaces(arg_space):
        """
        TO BE MOVED TO A UTILS MODULE!
        """
        if not isinstance(arg_space, tuple):
            arg1_space = arg_space
            arg2_space = arg_space
        else:
            if len(arg_space) != 2:
                raise ValueError("expected two spaces, received %d-ary tuple "
                                 % len(arg_space))
            arg1_space, arg2_space = arg_space

        assert_is_instance(arg1_space, Space)
        assert_is_instance(arg2_space, Space)

        assert_space_match(arg1_space, arg2_space)

        return arg1_space, arg2_space

def assert_space_match( arg1_space, arg2_space, phrase_space=None):

        if arg1_space.id2column != arg2_space.id2column:
            raise ValueError("Argument spaces do not have identical columns!")

        if not phrase_space is None:
            if arg1_space.id2column != phrase_space.id2column:
                raise ValueError("Argument and phrase space do not have identical columns!")

def valid_data_to_lists( data, (row2id1, row2id2, row2id3)):
        """
        TO BE MOVED TO A UTILS MODULE!
        """
        list1 = []
        list2 = []
        list3 = []

        j = 0
        for i in xrange(len(data)):
            sample = data[i]

            cond = True

            if not row2id1 is None:
                cond = cond and sample[0] in row2id1

            if not row2id2 is None:
                cond = cond and sample[1] in row2id2

            if not row2id3 is None:
                cond = cond and sample[2] in row2id3

            if cond:
                list1.append(sample[0])
                list2.append(sample[1])
                list3.append(sample[2])
                j += 1
            else:
                #this means the data (phrase or the unigram) is missing
                print 'training data is missing: check ' + str(sample) 
                #this is a great place to check the matching of data - that is if the 
                #training data exists or not - but for the time being we can leave this part 
                #and continue with our experiments 
                list1.append(sample[0])
                list2.append(sample[1])
                list3.append(sample[2])
                j += 1


        if i + 1 != j:
            warn("%d (out of %d) lines are ignored because one of the elements is not found in its semantic space"
                 % ((i + 1) - j, (i + 1)))

        if not list1:
            raise ValueError("No valid data found for training/composition!")

        return list1, list2, list3

    
def weightedAverageTrain(arg1_space, arg2_space, phrase_space, arg1_list, arg2_list, phrase_list):
    
    chunk_size = int(phrase_space.cooccurrence_matrix.shape[0] * MAX_MEM_OVERHEAD / 3.0) + 1
    arg1_arg2_dot, arg1_phrase_dot, arg2_phrase_dot, arg1_norm_sqr, arg2_norm_sqr = (0, 0, 0, 0, 0)

    for i in range(int(math.ceil(len(arg1_list) / float(chunk_size)))):
        beg, end = i*chunk_size, min((i+1)*chunk_size, len(arg1_list))

        arg1_mat = arg1_space.get_rows(arg1_list[beg:end])
        arg2_mat = arg2_space.get_rows(arg2_list[beg:end])
        phrase_mat = phrase_space.get_rows(phrase_list[beg:end])

        #checking of dense/sparse thing
        [arg1_mat, arg2_mat, phrase_mat] = resolve_type_conflict([arg1_mat,
                                                                      arg2_mat,
                                                                      phrase_mat],
                                                                      DenseMatrix)

        res = weightedAverageProcess(arg1_mat, arg2_mat, phrase_mat)
        arg1_arg2_dot += res[0]
        arg1_phrase_dot += res[1]
        arg2_phrase_dot += res[2]
        arg1_norm_sqr += res[3]
        arg2_norm_sqr += res[4]


        weightedAverageSolve(arg1_arg2_dot, arg1_phrase_dot, arg2_phrase_dot, arg1_norm_sqr, arg2_norm_sqr)


def weightedAverageProcess( arg1_mat, arg2_mat, phrase_mat):

        # debug here
        # remove when done
        # print "Using %s MB " % (get_mem_usage())
        
    print 'arg1 mat shape ' + str(arg1_mat.mat.shape)
    print 'arg2 mat shape ' + str(arg2_mat.mat.shape)
    print 'phrase mat shape ' + str(phrase_mat.mat.shape)
    

    arg1_arg2_dot = arg1_mat.multiply(arg2_mat).sum()
    arg1_phrase_dot = arg1_mat.multiply(phrase_mat).sum()
    arg2_phrase_dot = arg2_mat.multiply(phrase_mat).sum()

    arg1_norm_sqr = pow(arg1_mat.norm(), 2)
    arg2_norm_sqr = pow(arg2_mat.norm(), 2)

    return arg1_arg2_dot, arg1_phrase_dot, arg2_phrase_dot, arg1_norm_sqr, arg2_norm_sqr

def weightedAverageSolve( arg1_arg2_dot, arg1_phrase_dot, arg2_phrase_dot, arg1_norm_sqr, arg2_norm_sqr):

    a = np.linalg.pinv(np.mat([[arg1_norm_sqr,arg1_arg2_dot],
                                   [arg1_arg2_dot,arg2_norm_sqr]]))
    a = a * np.mat([[arg1_phrase_dot],[arg2_phrase_dot]])
    alphaParam = a[0, 0]
    betaParam = a[1, 0]

    print 'alpha = ' + str(alphaParam) + ' beta = ' + str(betaParam)
    

if __name__ == '__main__':
    
    #trainingWeightedAverageModel()
    trainingWeightedAverageModelHere()
   
