import time
import datetime

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
from composes.utils.regression_learner import RegressionLearner
from composes.utils.regression_learner import LstsqRegressionLearner
from composes.utils.matrix_utils import padd_matrix



import logging
from composes.utils import log_utils as log

logger = logging.getLogger(__name__)

#mainPath = '/Users/dg513/work/eclipse-workspace/compose-workspace/dissect/demo/'
mainPath = '/export/home/dghosh/work/compos/project/data/dissect/demo/'

MAX_MEM_OVERHEAD = 0.2
composed_id2column = None
has_intercept = False

regression_learner = LstsqRegressionLearner()

alphaParam = 0
betaParam = 0 
mat_a_t = None
mat_b_t = None
    
def trainingFullAdditiveModel():
    
    st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
    print 'start time of training: ' + st

    
    core_cooccurrence_file  = mainPath + 'core.sm.verysmall'
    core_row_file = mainPath + 'core.rows'
    core_col_file = mainPath + 'core.cols'
    core_space_file = mainPath + 'core.pkl'
 
    per_cooccurrence_file  = mainPath + 'sv.sm.verysmall'
    per_row_file = mainPath + 'sv.rows'
    per_col_file = mainPath + 'sv.cols'
    
    per_space_file = mainPath + 'sv.pkl'
    core_space_file = mainPath + "core.pkl"

    training_pair_file = mainPath + "training_pairs_small.txt"
    testing_pair_file = mainPath + "testing_pairs.txt"
    composed_space_file = mainPath + "composed.pkl"
    
    # config log file
   # log_utils.config_logging(log_file)
    
    train_data = io_utils.read_tuple_list(training_pair_file, fields=[0,1,2])
    
    print "Reading done in train data"
    st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
    print 'reading done time of training: ' + st

    print "Training full additive Function compositional model"
 #   core_space = io_utils.load(core_space_file)
 #   per_space = io_utils.load(sv_space_file)
    
    core_space = Space.build(data=core_cooccurrence_file, rows=core_row_file,
                                 cols=core_col_file, format="sm")
  
    per_space = Space.build(data=per_cooccurrence_file, rows=per_row_file,
                                 cols=per_col_file, format="sm")
    
 #   per_space = PeripheralSpace.build(core_space,data=per_cooccurrence_file, cols=per_col_file,
  #                                    rows=per_row_file, format="sm")
  
#    print per_space.id2row
 #   print per_space.id2column
  #  print per_space.row2id
   # print per_space.column2id
   # print per_space._cooccurrence_matrix
    
    print core_space.id2row
    print core_space.id2column
    

    
  
    print 'loaded the core and the extra phrase data corpus'
    
    comp_model = FullAdditive()
    train(train_data, core_space, per_space)
    
    print 'finished training on full additive model'
    st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
    print 'finished training time of training: ' + st

    print "Finding neighbors of \"conflict-n_erupt-v\" in the composed space"
   # composed_space = comp_model.compose([("erupt-v", "conflict-n","conflict-n_erupt-v")], core_space)
    test_phrases = io_utils.read_tuple_list(testing_pair_file, fields=[0,1,2])
    composed_space = compose(test_phrases, core_space)
    neighbors = composed_space.get_neighbours("conflict-n_erupt-v", 10, CosSimilarity())
    print neighbors
    
    st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
    print 'finished composing/finding neighbors time of training: ' + st

   
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
   
     #interceptor is the constant. like y = mx + c (c is the interceptor)
     
     
     [mat_a_t, mat_b_t, arg1_mat] = resolve_type_conflict([mat_a_t,
                                                              mat_b_t,
                                                              arg1_mat],
                                                             type(arg1_mat))
     
     if has_intercept:
         return arg1_mat * mat_a_t + padd_matrix(arg2_mat, 1) * mat_b_t
     else:
        return arg1_mat * mat_a_t + arg2_mat * mat_b_t

def build_id2column( arg1_space, arg2_space):
        return arg1_space.id2column

def train(train_data, arg_space, phrase_space):
    
    start = time.time()

    #they look same because we are learning from the same space of row-column (target/neighbours)
    arg1_space, arg2_space = extract_arg_spaces(arg_space)
    ids1 =arg1_space.row2id
    ids2 =arg2_space.row2id
    
    print 'length of the argument space is ' + str(len(ids1))
    print ids1
    print 'value of address is ' + str(ids1['address-n'])
    
    arg1_list, arg2_list, phrase_list = valid_data_to_lists(train_data,
                                                                     (arg1_space.row2id,
                                                                      arg2_space.row2id,
                                                                      phrase_space.row2id)
                                                                     )

    fullAdditiveTrain(arg1_space, arg2_space, phrase_space, arg1_list, arg2_list, phrase_list)
    
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

        if i + 1 != j:
            warn("%d (out of %d) lines are ignored because one of the elements is not found in its semantic space"
                 % ((i + 1) - j, (i + 1)))

        if not list1:
            raise ValueError("No valid data found for training/composition!")

        return list1, list2, list3

    
def fullAdditiveTrain(arg1_space, arg2_space, phrase_space, arg1_list, arg2_list, phrase_list):
    
    arg1_mat = arg1_space.get_rows(arg1_list)
    arg2_mat = arg2_space.get_rows(arg2_list)
    phrase_mat = phrase_space.get_rows(phrase_list)
    
    print arg1_mat
    print arg2_mat
    print phrase_mat

    [arg1_mat, arg2_mat, phrase_mat] = resolve_type_conflict([arg1_mat,
                                                                  arg2_mat,
                                                                  phrase_mat],
                                                                  DenseMatrix)


    fullAdditiveSolve(arg1_mat, arg2_mat, phrase_mat)


def fullAdditiveProcess( arg1_mat, arg2_mat, phrase_mat):

        # debug here
        # remove when done
        # print "Using %s MB " % (get_mem_usage())

    arg1_arg2_dot = arg1_mat.multiply(arg2_mat).sum()
    arg1_phrase_dot = arg1_mat.multiply(phrase_mat).sum()
    arg2_phrase_dot = arg2_mat.multiply(phrase_mat).sum()

    arg1_norm_sqr = pow(arg1_mat.norm(), 2)
    arg2_norm_sqr = pow(arg2_mat.norm(), 2)

    return arg1_arg2_dot, arg1_phrase_dot, arg2_phrase_dot, arg1_norm_sqr, arg2_norm_sqr

    
def fullAdditiveSolve( arg1_mat, arg2_mat, phrase_mat):

    global has_intercept
    has_intercept = regression_learner.has_intercept()
    
    
    result = regression_learner.train(arg1_mat.hstack(arg2_mat), phrase_mat)
    
    print result.get_mat()
    global mat_a_t
    global mat_b_t 
    
    mat_a_t = result[0:arg1_mat.shape[1], :]
    mat_b_t = result[arg1_mat.shape[1]:, :]
    
if __name__ == '__main__':
    
    trainingFullAdditiveModel()
