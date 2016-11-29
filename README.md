# Innopolis Information Retrieval 2016 Semester Project

## Project Structure
The project divided into 3 modules:

- **crawler** - Wikipedia's articles crawler

- **core** - main module which implements documents search system

- **web** - google-like web interface for interacting with search system from **core** module

### Modules description

**crawler** provides command-line interface (cli) for launching crawling process
which asks for path to folder in which it will create subfolder _documents_ and will be putting 
crawled documents into this subfolder. 

**core** consist of 3 main parts (packages):

- _preprocessing_ - implementation of documents and queries normalization 
(tokenization, tokens lemmatization and word/numbers filtering). 
Based on [Stanford Core-NLP](http://stanfordnlp.github.io/CoreNLP/) library.

- _index_ - implementation of Vector Space Model for ranked retrieval. 
Contains 2 implementations of Vector Space Model indexes (both based on inverted index): 
InMemory (`ru.innopolis.ir.project.core.index.VectorSpaceModelInMemoryIndex`), 
which stores postings lists in main memory (RAM), 
and File-based (`ru.innopolis.ir.project.core.index.VectorSpaceModelIndex`), 
which store postings lists of all terms in a single file. 
File-based is used by default, because it can handle arbitrary number of documents. 

- _rest_ - simple REST service which coordinates normalization and indexing processes, 
and handle requests from user-interface for documents search. 
As well as **web** module, based on [Scalatra](http://www.scalatra.org/) 
framework for defining controllers and http server [Jetty](http://www.eclipse.org/jetty/). 
Requires such command line arguments for launching:
	- path to folder, in which REST service will perform it's maintenance work:
	 
	  - periodically normalizing documents coming to _documents_ subfolder 
	  and saving them into *new_normalized_docs* subfolder
	
      - periodically rebuilding search index when there is sufficient amount 
      (provided as a command line argument) of new normalized documents 
      in *new_normalized_docs* subfolder (moving them into *indexed_docs* subfolder 
      before rebuilding to mark these documents as taken for processing), 
      removing old index instance postings file and serializing new instance 
      for fast future restart of service   
      
   - delay for periodic checking for new documents to normalize them
   
   - delay for periodic checking for new normalized docs to rebuild index
   
   - minimal number of new normalized documents to start rebuilding index
   
   - port number on which REST server should listen for coming documents search requests

### Modules interaction

**crawler** and **core** modules interact in quite obvious way: 
launched as separate processes (**crawler**'s cli and **core**'s REST service)
but provided with path to same folder

- **crawler** puts new documents into _documents_ subfolder  

- **core** periodically handles new documents from _documents_, normalizes them and 
from time to time builds new index with new documents provided by crawler

**web** and **core**, also launched as separate processes, interact in such way:

- **web** accepts search queries from users and sends them to **core**'s REST service 

- **core** uses the most fresh search index instance to perform search 
and returns top documents as answer to **web**'s search request

- **web** accepts result from **core** and shows it in user-friendly way to user

## How to run
To run our system you need nothing more except [Scala](https://www.scala-lang.org/) and 
[SBT](http://www.scala-sbt.org/) installed. All interactions with modules' functionalities will 
be performed using **SBT**.
 
1. Choose some folder as working directory. Let's call path to it as *\<work_dir\>*.

2. Open 3 separate instances of console.

3. In first instance type `sbt "project crawler" "run -o <work_dir> -t 4"`. It is a quickest way to launch crawler.

   You can also type `sbt crawler/run` to get such parameters details:
```text
Usage: wikicrawler [options]

  -o, --out <value>       Working directory
  -t, --threads <value>   Number of working threads
  -i, --Interval <value>  Console output interval (in seconds)
  -c, --count <value>     Sets how many documents to crawl
```

4. In second instance of console type `sbt "project core" "run <work_dir>"`. 
It will start **core**'s REST service with *<work\_dir>* as maintenance folder.

	When starting **core**'s REST service there are 5 possible scenarios:
	
	1. There is a file *last_index.obj* which contains serialized last created instance of search index =\>
	
		Last created instance will be deserialized and applied for service start. 
		 **Important note**: it is assumed, that if there is such *last_index.obj*, 
		 then there should exists corresponding postings lists file 
		 (name starts with *index_postings_file_*) and *indexed_docs* 
		 should contain all indexed docs. I.e. you must not change 
         *index_postings_file_* nor *indexed_docs*. If you changed any of them, 
	     you should remove *last_index.obj*.
	   
	2. There is no *last_index.obj* but *indexed_docs* folder is not empty =\>
	
		Index will be build using all documents from *indexed_docs* and serialized.
		 
	3. There is no *last_index.obj*, *indexed_docs* empty or doesn't exists but 
	*new_normalized_docs* is not empty =\>
	
		All normalized documents from *new_normalized_docs* will be moved to *indexed_docs*, 
		and index will be build using documents from *indexed_docs*.
		
	4. There is no *last_index.obj*, *indexed_docs* is empty or doesn't exist, 
	*new_normalized_docs* is empty or doesn't exist, but *documents* folder not empty =\>
	
		All documents from *documents* will be normalized, moved to *indexed_docs*, 
		and index will be build using documents from *indexed_docs*.
		
	5. There is no *last_index.obj*, *documents*, *new_normalized_docs* and *indexed_docs* 
	are empty or don't exists =\> 
	    
	    Index will not be created. REST service will start, but search will return empty result 
	    until some documents appear in *document* folder (in such cases you will see such). 
	    
	When starting scenario is done, two periodic tasks will be configured:
    
    - [Docs Normalization Task] monitors *documents* folder with \<norm_delay\> (all parameters are described below) delay 
     for new documents to normalize them and save normalized versions in *new_normalized_docs* folder
     
    - [Index Rebuilding Task] monitors *new_normalized_docs* folder with \<index_delay\> delay and 
    if the number of files there is at least \<min_to_rebuild\>: 
    
      1. moves all doc files from *new_normalized_docs* to *indexed_docs* to mark them as processed
      
      2. builds new instance of index parsing documents from *indexed_docs* in single pass
         (old instance works until new one is not built)
        
      3. serializes new index to file *last_index.obj* for fast future
      
      4. replaces old instance with new in thread-safe manner
       
      5. removes old index instance postings file when all queries working at the moment 
      with old instance are done
	    
	Here is a description of **core**'s REST service parameters which you can get by typing 
	`sbt core/run`:
	
```text
Usage: coreRestService [options] <working_dir>

  <working_dir>            Path to the working folder of Search System. 
  -n, --norm_delay <value>
                           New documents normalization delay in format `<length><unit>` with <unit> one of: 'd' - days, 'h' - hours, 'min' - minutes, 's' - seconds, 'ms' - milliseconds. Default - '3s'
  -i, --index_delay <value>
                           Delay of checking for new normalized docs to rebuild index. Format is same as for 'norm_delay', but the minimal unit is 's' - seconds. Default - '15s'
  -m, --min_to_rebuild <value>
                           Minimal number of new normalized documents for rebuilding index. Default - 1000.
  -p, --port <value>       Port on which start listening search requests. Default - 8081.
  -t, --norming_threads <value>
                           Number of threads to use for documents normalization. Default - number of your computer's cores divided by 2.

```
	    
5. In 3rd instance of console type `sbt`. It will start **SBT**'s console. 
Then type `web/jetty:start`. It will start web-interface server. 

6. Go to [http://localhost:8080](http://localhost:8080)

7. Enjoy our search system :\)