# Course-Gradebook

Data is everywhere in table form: spreadsheets, bank statements, sports stats, and scientific measurements. This project models tables using sequences (lists), enabling operations such as:

Reading and writing CSV files

Manipulating tables (e.g., transpose)

Computing summary statistics (mean and standard deviation)

Appending row- and column-level summaries

Two implementations of a custom Seq interface are provided:

ArraySeq – backed by a dynamic array.

DLinkedSeq – backed by a doubly-linked list with iterators.

The application shows how abstract data types can be implemented efficiently and tested for correctness.

Features
1. Seq Interface

Supports dynamic insertion and removal of elements.

Iteration through Iterator interface.

Implemented both as ArraySeq and DLinkedSeq.

Includes defensive programming checks (assertInv) to maintain invariants.

2. CSV Handling

constructTable(String path) – reads a simplified CSV into a Seq<Seq<String>>.

outputCSV(String path, Seq<Seq<String>> table) – writes nested sequences to a CSV file.

Supports rectangular tables; ragged tables are not guaranteed.

3. Table Manipulation

transpose(Seq<Seq<T>> table) – flips rows and columns.

Ensures operations are efficient by using iterators.

4. Summary Statistics

powerSum(Seq<String> seq, int n) – computes nth power sum of numerical entries.

mean(Seq<String> seq) – average of numerical entries.

stdDev(Seq<String> seq) – standard deviation of numerical entries.

addSummaryColumns(Seq<Seq<String>> table) – appends mean and standard deviation columns for each row.

addSummaryRows(Seq<Seq<String>> table) – appends mean and standard deviation rows for each column.
