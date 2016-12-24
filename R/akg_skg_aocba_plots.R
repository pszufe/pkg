library(plotrix)
library(reshape2)
library(tikzDevice)
library(sqldf)


#--,obsAvailDist,obsAvailDistParam,exmperimDist,experimTime


#View(dataset)

get_d <- function(alg,N=5,a__m=1, oAD='none',eD='const',data__=dataset) {
  a=1  #total number of experimenters
  m=1  #number of slots per experimenter
  if (substr(alg,1,1) == "A") {
    a = a__m
  } else {
    m = a__m
  }
  res = data__[data__$alg==alg & data__$N==N & data__$a==a & data__$m==m & data__$obsAvailDist==oAD & data__$exmperimDist==eD & data__$noExperiments<=80 & data__$noExperiments>=0,]
  if (nrow(res)==0) {
    stop("NROW=0")
  }
  return(res)
}

get_d_normalized <- function(alg,...) {
  dm <- merge(get_d(alg,...),get_d("OCBA99",a__m=1),by="noExperiments",suffixes=c("",".99"))
  dm$avg_bestBinary <- dm$avg_bestBinary-dm$avg_bestBinary.99
  dm$avg_bestDistance <- dm$avg_bestDistance/dm$avg_bestDistance.99
  return(dm[,1:((ncol(dm)+1)/2)])  
}




plotsc <- function(types,lnames=types,normalize=TRUE,a__m=replicate(length(types),1)) {
  #tikz(paste("tex/plot",no,".tex",width = 4,height=4)
  colors=c("red","magenta","blue","green","cyan")
  par(mar=c(4.1,4.1,4.1,2.1))
  d <- get_d_normalized(types[1],a__m=a__m[1])
  d$avg_ocba
  plot(d$noExperiments, d$avg_bestBinary,type="l",col=colors[1],xlab="experiments",ylab="Lift od probability of correct selection over a na\\\"ive approach",lty=1)
  if (length(types)>1) {
    print(types)
    for (i in 2:length(types)) {
      d <- get_d_normalized(types[i],a__m=a__m[i])    
      lines(d$noExperiments, d$avg_bestBinary,col=colors[i],lty=2)
    }
  }
  legend(40,0.02, legend=lnames,col=colors[1:length(types)],lty=1:length(types))
}

setwd("C:\\!BIBLIOTEKA\\WinterSim\\AKG")

dataset <- read.csv2("wyniki_symulacji_155090_N=10.txt", sep="\t",dec=".")
plotsc(c("KG","SKG","AOCBA2","OCBA2","AKG","KG"),a__m=c(1,10,10,10,10))
View(sqldf("SELECT alg,N,a,m, SUM(1) FROM dataset GROUP BY alg,N,a,m"))



dataset <- read.csv2("wyniki_symulacji_147880_N=5.txt", sep="\t",dec=".")
plotsc(c("KG","SKG","AOCBA2","OCBA2","AKG"),a__m=c(1,5,5,5,5))

tikz(paste("tex/plot",1,".tex",sep=""),width = 4,height=4)
plotsc(c("KG","OCBA2"),a__m=c(1,1,1,1,1))
dev.off()
#"AOCBA2","OCBA2","AKG"

