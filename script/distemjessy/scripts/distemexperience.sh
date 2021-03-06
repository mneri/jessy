#!/bin/bash

source /root/distemjessy/scripts/configuration.sh
#source /home/msaeida/jessy_script/configuration.sh

param=("$@")


function fetchExecutionResult(){

	echo "Files from server already present. Fetching files from clients ... "
	for tu in "${clients[@]}"
	do
		distem --copy-from vnode=${tu},src=/root/distemjessy/scripts/${tu},dest=/root/distemjessy/scripts
	done
	#echo "Fetching files from servers"
	#for tu in "${servers[@]}"
        #do
                #distem --copy-from vnode=${tu},src=/root/distemjessy/scripts/${tu},dest=/root/distemjessy/scripts
        #done

}




function syncConfig(){

	echo 'synchronising configuration across sites ...'	
	for tu in "${nodes[@]}"
	do
		(distem --execute vnode=${tu},command="rm -rf /root/distemjessy/scripts/configuration.sh"; distem --copy-to vnode=${tu},src=/root/distemjessy/scripts/configuration.sh,dest=/root/distemjessy/scripts/configuration.sh) &
		pids="$pids $!"
	done
	
	for p in "${pids[@]}"
        do
                wait ${p};
        done
             	
}

function stopExp(){
    let sc=${#servers[@]}-1
    for j in `seq 0 $sc`
    do
	#distem --execute vnode=${servers[$j]},command="ps -ef | grep java | awk '{print $2}' | xargs kill -SIGTERM" 2&>1 > /dev/null
	nohup ${SSHCMD} -o 'StrictHostKeyChecking no' ${servers[$j]} "ps -ef | grep java | awk '{print \$2}' | xargs kill -SIGTERM" 2&>1 > /dev/null &
    done

    sleep 5

    let e=${#nodes[@]}-1
    for i in `seq 0 $e`
    do
	echo "stopping on ${nodes[$i]}"
	#distem --execute vnode=${nodes[$i]},command="ps -ef | grep java | awk '{print $2}' | xargs kill -9" 2&>1 > /dev/null

	nohup ${SSHCMD} -o 'StrictHostKeyChecking no' ${nodes[$i]} "ps -ef | grep java | awk '{print \$2}' | xargs kill -9" 2&>1 > /dev/null &
    done

}

function dump(){
    let c=${#clients[@]}-1
    for j in `seq 0 $c`
    do
	echo "stopping on ${clients[$j]}"
	#distem --execute vnode=${clients[$j]},command="ps -ef | grep java | awk '{print $2}' | xargs kill -SIGQUIT" 2&>1 > /dev/null
	#distem --execute vnode=${clients[$j]},command="wait 5" 2&>1 > /dev/null
	#distem --execute vnode=${clients[$j]},command="ps -ef | grep java | awk '{print $2}' | xargs kill -9" 2&>1 > /dev/null
	nohup ${SSHCMD} -o 'StrictHostKeyChecking no' ${clients[$j]} "ps -ef | grep java | awk '{print \$2}' | xargs kill -SIGQUIT \
			 		&& wait 5 \
					&& ps -ef | grep java | awk '{print \$2}' | xargs kill -9" 2&>1 > /dev/null &
    done


}

function collectStats(){
    overallThroughput=0;
    committedThroughput=0;
    runtime=0;
    updateLatency=0
    readLatency=0
    consistency=${cons[$selectedCons]} #`grep 'consistency_type\ =' config.property | awk -F '=' '{print $2}'`
    failedTerminationRatio=0;
    failedExecutionRatio=0;
    failedReadsRatio=0;
    timeoutRatio=0;
    executionTime_readonly=0;
    executionTime_update=0;
    terminationTime_readonly=0;
    terminationTime_update=0;
    votingTime=0;
    
    certificationTime_readonly=0;
    certificationTime_update=0;
    certificationQueueingTime=0;
    applyingTransactionQueueingTime=0;
    

	if ! [ -s "${scriptdir}/results/${servercount}.txt" ]; then
	    echo -e  "Consistency\tServer_Machines\tClient_Machines\tNumber_Of_Clients\tOverall_Throughput\tCommitted_Throughput\tupdateTran_Latency\treadonlyTran_Latency\tFailed_Termination_Ratio\tFailed_Execution_Ratio\tFailed_Read_Ratio\tTermination_Timeout_Ratio\tExecutionLatency_UpdateTran\tupdateCertificationLatency\tExecutionLatency_ReadOnlyTran\treadonlyCertificationLatency\tVoting_Time\tCertificationLatency_UpdateTran\tCertificationLatency_readonlyTran\tCertificationQueueingTime\tApplyingTransactionQueueingTime"
	fi

    let scount=${#servers[@]}-1
    for j in `seq 0 $scount`
    do
	server=${servers[$j]}

	tmp=`grep -a "certificationTime_readonly" ${scriptdir}/${server} | nawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    certificationTime_readonly=`echo "${tmp}+${certificationTime_readonly}"| sed 's/E/*10^/g'`;	    
	fi

	tmp=`grep -a "certificationTime_update" ${scriptdir}/${server} | nawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    certificationTime_update=`echo "${tmp}+${certificationTime_update}"| sed 's/E/*10^/g'`;	    
	fi

	tmp=`grep -a "certificationQueueingTime_update" ${scriptdir}/${server} | nawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    certificationQueueingTime=`echo "${tmp}+${certificationQueueingTime}"| sed 's/E/*10^/g'`;	    
	fi

	tmp=`grep -a "applyingTransactionQueueingTime_update" ${scriptdir}/${server} | nawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    applyingTransactionQueueingTime=`echo "${tmp}+${applyingTransactionQueueingTime}"| sed 's/E/*10^/g'`;	    
	fi

    done


    let e=${#clients[@]}-1
    for i in `seq 0 $e`
    do

	client=${clients[$i]}

	tmp=`grep -a Throughput ${scriptdir}/${client} | nawk -F',' '{print $3}'`;
	if [ -n "${tmp}" ]; then
	    overallThroughput=`echo "${tmp} + ${overallThroughput}" | ${bc}`;
	fi

	runtime=`grep -a RunTime ${scriptdir}/${client} | nawk -F',' '{print $3}'`;
	tmp=`grep -a "Return=0" ${scriptdir}/${client} | awk -F "," '{sum+= $3} END {print sum}'`;
	if [ -n "${tmp}" ]; then
	    committedThroughput=`echo "((1000*${tmp})/${runtime}) + ${committedThroughput}" | ${bc}`;
	fi

	tmp=`grep -a "\[UPDATE\], AverageLatency" ${scriptdir}/${client} | nawk -F',' '{print $3}'`;
	if [ -n "${tmp}" ]; then
	    updateLatency=`echo "${tmp} + ${updateLatency}" | ${bc}`;
	fi

	tmp=`grep -a "\[READ\], AverageLatency" ${scriptdir}/${client} | nawk -F',' '{print $3}'`;
	if [ -n "${tmp}" ]; then
	    readLatency=`echo "${tmp} + ${readLatency}" | ${bc}`;
	fi
	
	tmp=`grep -a "ratioFailedTermination" ${scriptdir}/${client} | nawk -F':' '{print $2}'`;
	if [[ (! ${tmp} =~ '/') && (-n "${tmp}") ]]; then
	    failedTerminationRatio=`echo "${tmp}+${failedTerminationRatio}"| sed 's/E/*10^/g'` ;	    
	fi

	tmp=`grep -a "ratioFailedExecution" ${scriptdir}/${client} | nawk -F':' '{print $2}'`;
	if [[ (! ${tmp} =~ '/') && (-n "${tmp}") ]]; then
	    failedExecutionRatio=`echo "${tmp}+${failedExecutionRatio}"| sed 's/E/*10^/g'` ;	    
	fi


	tmp=`grep -a "ratioFailedReads" ${scriptdir}/${client} | nawk -F':' '{print $2}'`;
	if [[ (! ${tmp} =~ '/') && (-n "${tmp}") ]]; then
	    failedReadsRatio=`echo "${tmp}+${failedReadsRatio}"| sed 's/E/*10^/g'`;	    
	fi

	tmp=`grep -a "timeoutRatioAbortedTransactions" ${scriptdir}/${client} | nawk -F':' '{print $2}'`;
	if [[ (! ${tmp} =~ '/') && (-n "${tmp}") ]]; then
	    timeoutRatio=`echo "${tmp}+${timeoutRatio}"| sed 's/E/*10^/g'`;	    
	fi

	####################################################################################################
	####################################START OF PROBES IN TRANSACTION##################################
	tmp=`grep -a "transactionExecutionTime_ReadOlny" ${scriptdir}/${client} | nawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    executionTime_readonly=`echo "${tmp}+${executionTime_readonly}"| sed 's/E/*10^/g'`;	    
	fi
	
	tmp=`grep -a "transactionExecutionTime_Update" ${scriptdir}/${client} | nawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    executionTime_update=`echo "${tmp}+${executionTime_update}"| sed 's/E/*10^/g'`;	    
	fi

	tmp=`grep -a "transactionTerminationTime_ReadOnly" ${scriptdir}/${client} | nawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    terminationTime_readonly=`echo "${tmp}+${terminationTime_readonly}"| sed 's/E/*10^/g'`;	    
	fi
	
	tmp=`grep -a "transactionTerminationTime_Update" ${scriptdir}/${client} | nawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    terminationTime_update=`echo "${tmp}+${terminationTime_update}"| sed 's/E/*10^/g'`;	    
	fi

	tmp=`grep -a "votingTime" ${scriptdir}/${client} | nawk -F':' '{print $2}'`;
	if [ -n "${tmp}" ]; then
	    votingTime=`echo "${tmp}+${votingTime}"| sed 's/E/*10^/g'`;	    
	fi


    done
    
    overallThroughput=`echo "scale=2;(${overallThroughput})/1" | ${bc} `;
    committedThroughput=`echo "scale=2;${committedThroughput}" | ${bc} `;

    updateLatency=`echo "scale=2;(${updateLatency})/${#clients[@]}" | ${bc}`;
    readLatency=`echo "scale=2;(${readLatency})/${#clients[@]}" | ${bc}`;
    clientcount=`echo "${#clients[@]}*${t}" | ${bc}`;

    failedTerminationRatio=`echo "scale=10;(${failedTerminationRatio})/${#clients[@]}" | ${bc}`;
    failedExecutionRatio=`echo "scale=10;(${failedExecutionRatio})/${#clients[@]}" | ${bc}`;

    failedReadsRatio=`echo "scale=10;(${failedReadsRatio})/${#clients[@]}" | ${bc}`;
    timeoutRatio=`echo "scale=10;(${timeoutRatio})/${#clients[@]}" | ${bc}`;

    certificationTime_readonly=`echo "scale=2;(${certificationTime_readonly})/${#servers[@]}" | ${bc}`;
    certificationTime_update=`echo "scale=2;(${certificationTime_update})/${#servers[@]}" | ${bc}`;

    certificationQueueingTime=`echo "scale=2;(${certificationQueueingTime})/${#servers[@]}" | ${bc}`;
    applyingTransactionQueueingTime=`echo "scale=2;(${applyingTransactionQueueingTime})/${#servers[@]}" | ${bc}`;

    executionTime_readonly=`echo "scale=2;(${executionTime_readonly})/${#clients[@]}" | ${bc}`;
    executionTime_update=`echo "scale=2;(${executionTime_update})/${#clients[@]}" | ${bc}`;
    terminationTime_readonly=`echo "scale=2;(${terminationTime_readonly})/${#clients[@]}" | ${bc}`;
    terminationTime_update=`echo "scale=2;(${terminationTime_update})/${#clients[@]}" | ${bc}`;
    votingTime=`echo "scale=2;(${votingTime})/${#clients[@]}" | ${bc}`;
    
    echo -e  "${consistency}\t${servercount}\t$[${#clients[@]}]\t${clientcount}\t${overallThroughput}\t${committedThroughput}\t${updateLatency}\t${readLatency}\t${failedTerminationRatio}\t${failedExecutionRatio}\t${failedReadsRatio}\t${timeoutRatio}\t${executionTime_update}\t${terminationTime_update}\t${executionTime_readonly}\t${terminationTime_readonly}\t${votingTime}\t${certificationTime_update}\t${certificationTime_readonly}\t${certificationQueueingTime}\t${applyingTransactionQueueingTime}"

}


trap "stopExp; wait; exit 255" SIGINT SIGTERM
trap "dump; wait;" SIGQUIT

 ##############
 # Experience #
 ##############
let servercount=${#servers[@]}

let consCount=${#cons[@]}-1

#touch /root/.ssh/config
#echo "Host *" >> /root/.ssh/config
#echo "    StrictHostKeyChecking no" >> /root/.ssh/config

#let e=${#nodes[@]}-1
#for i in `seq 0 $e`
#do
	#distem --execute vnode=${nodes[$i]},command="touch /root/.ssh/config"
	#distem --execute vnode=${nodes[$i]},command="rm /root/.ssh/known_hosts"
	#distem --execute vnode=${nodes[$i]},command="echo \"Host *\" >> /root/.ssh/config"
	#distem --execute vnode=${nodes[$i]},command="echo \"    StrictHostKeyChecking no\" >> /root/.ssh/config"
	#distem --execute vnode=${nodes[$i]},command="service ssh restart"
#done


for selectedCons in `seq 0 $consCount`
do  

	sed -i "s/consistency_type.*/consistency_type\ =\ ${cons[$selectedCons]}/g" config.property

	#thread setup
	thread=()

	for i in $(seq 0 $(expr ${#client_thread[@]} - 2));
		do
			temp=$(expr ${client_thread[i+1]} - ${client_thread_increment[i]})
			thread1=`seq ${client_thread[i]} ${client_thread_increment[i]} $temp`
			thread=( ${thread[@]} ${thread1[@]} )
		done

	thread=( ${thread[@]} ${client_thread[$(expr ${#client_thread[@]} - 1)]} )


	for t in ${thread[@]}; 
	do


	#############WE LOOP HERE UNTIL CLAUNCHER_SUCCEED=1
	CLAUNCHER_SUCCEED=3
	while [ $CLAUNCHER_SUCCEED -eq 3 ]; do
	# 0 - Starting the server
		
	echo "removing previous outputs."
	rm -f *.fr*

	    echo "Starting servers ..."
	    sed -i 's/-t/-load/g' configuration.sh
	    sed -i "s/nthreads.*/nthreads=1/g" configuration.sh

		syncConfig

	    sleep 10

	    ${scriptdir}/launcher.sh &


	# 1 - Loading phase
#	    echo "Loading phase ..."
#	    ${SSHCMD} ${clients[0]} "${scriptdir}/client.sh" > ${scriptdir}/loading

#	    sleep 30

	# 2 - Benchmarking phase

	    echo "Benchmarking phase ..."
	    sed -i 's/-load/-t/g' configuration.sh

	    sed -i "s/nthreads.*/nthreads=${t}/g" configuration.sh
	    echo "using ${t} thread(s) per machine"   

		syncConfig

	    echo "Waiting Before Launching Clients"
	    sleep 30
	    echo "Launching Clients"

	    /${scriptdir}/clauncher.sh
	    CLAUNCHER_SUCCEED=$?

	    stopExp


        
            sleep 20

            echo "trnasfering experiment files to the main launcher frontend..."
            let sc=${#servers[@]}-1
            #for ii in `seq 0 $sc`;
            #do
            #	scpServer=${servers[${ii}]}
            #	fetchExecutionResult ${scpServer}
            #done

            #let cc=${#clients[@]}-1
            #for ii in `seq 0 $cc`;
            #do
            #	scpClient=${clients[${ii}]}
            #	fetchExecutionResult ${scpClient}
            #done
            
	    fetchExecutionResult

	    echo "using ${t} thread(s) per machine is finished. Collecting stats"   
	    collectStats >>  ${scriptdir}/results/${servercount}.txt
#               source collectMeasurements.sh

	    sleep 10
	    
	done
	done
done
