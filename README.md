# Description
This is a sample project which calculates the measurements like Max, Min, Median, Normalization, and 90th percentile of a huge dataset.
Without using Hadoop MapReduce, this process will need too much time. By using features like combiner in MapReduce, we
can minimize the execution time of processing.
# Folder description
- MaxMin: This calculates the maximum and minimum of the dataset by using the combiner. In this case, we will have a better result in contrast to the MaxMin test folder.
- MaxMinTest: To be able to understand the advantages of the combiner, this folder calculates the maximum and minimum without using the combiner.
- Median-STD-Normalisation: Other measurements are calculated in this folder.
- Percentile: the 90th percentile of data is calculated.
