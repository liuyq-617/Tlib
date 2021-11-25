package org.devops

def pre_test(os="test"){
    if(os=='win'){
        bat '''
        taskkill /f /t /im python.exe
        cd C:\\
        rd /s /Q C:\\TDengine
        cd C:\\workspace\\TDinternal
        rd /s /Q C:\\workspace\\TDinternal\\debug
        cd C:\\workspace\\TDinternal\\community
        git reset --hard HEAD~10 
        '''
        script {
        if (env.CHANGE_TARGET == 'master') {
            bat '''
            cd C:\\workspace\\TDinternal\\community
            git checkout master
            '''
            }
        else if(env.CHANGE_TARGET == '2.0'){
            bat '''
            cd C:\\workspace\\TDinternal\\community
            git checkout 2.0
            '''
        } 
        else{
            bat '''
            cd C:\\workspace\\TDinternal\\community
            git checkout develop
            '''
        }
        }
        bat'''
        cd C:\\workspace\\TDinternal\\community
        git pull 
        git fetch origin +refs/pull/%CHANGE_ID%/merge
        git checkout -qf FETCH_HEAD
        git clean -dfx
        git submodule update --init --recursive
        cd C:\\workspace\\TDinternal
        git reset --hard HEAD~10
        '''
        script {
        if (env.CHANGE_TARGET == 'master') {
            bat '''
            cd C:\\workspace\\TDinternal
            git checkout master
            '''
            }
        else if(env.CHANGE_TARGET == '2.0'){
            bat '''
            cd C:\\workspace\\TDinternal
            git checkout 2.0
            '''
        } 
        else{
            bat '''
            cd C:\\workspace\\TDinternal
            git checkout develop
            '''
        } 
        }
        bat '''
        cd C:\\workspace\\TDinternal
        git pull 

        date
        git clean -dfx
        mkdir debug
        cd debug
        call "C:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\Community\\VC\\Auxiliary\\Build\\vcvarsall.bat" amd64
        cmake ../ -G "NMake Makefiles" 
        set CL=/MP nmake nmake || exit 8
        nmake install || exit 8
        xcopy /e/y/i/f C:\\workspace\\TDinternal\\debug\\build\\lib\\taos.dll C:\\Windows\\System32 || exit 8
        cd C:\\workspace\\TDinternal\\community\\src\\connector\\python
        python -m pip install .
        '''
    }else{
        sh'hostname'
        if (os == "test"){
            sh '''
            sudo rmtaos || echo "taosd has not installed"
            '''
            sh '''
            killall -9 taosd ||echo "no taosd running"
            killall -9 gdb || echo "no gdb running"
            killall -9 python3.8 || echo "no python program running"
            '''
        }
        sh '''
        cd ${WKC}
        git reset --hard HEAD~10 >/dev/null
        '''
        script {
        if (env.CHANGE_TARGET == 'master') {
            sh '''
            cd ${WKC}
            git checkout master
            '''
            }
        else if(env.CHANGE_TARGET == '2.0'){
            sh '''
            cd ${WKC}
            git checkout 2.0
            '''
        } 
        else{
            sh '''
            cd ${WKC}
            git checkout develop
            '''
        }
        }
        sh'''
        cd ${WKC}
        git pull >/dev/null
        git fetch origin +refs/pull/${CHANGE_ID}/merge
        git checkout -qf FETCH_HEAD
        git clean -dfx
        git submodule update --init --recursive
        cd ${WK}
        git reset --hard HEAD~10
        '''
        script {
        if (env.CHANGE_TARGET == 'master') {
            sh '''
            cd ${WK}
            git checkout master
            '''
            }
        else if(env.CHANGE_TARGET == '2.0'){
            sh '''
            cd ${WK}
            git checkout 2.0
            '''
        } 
        else{
            sh '''
            cd ${WK}
            git checkout develop
            '''
        } 
        }
        sh '''
        cd ${WK}
        git pull >/dev/null 

        export TZ=Asia/Harbin
        date
        git clean -dfx
        mkdir debug
        
        '''
        switch(os) {
            case "ningsi":
                sh '''
                    cd ${WK}/debug
                    cmake .. -DOSTYPE=Ningsi60 > /dev/null
                    make
                '''            
                break
            case "mac":
                sh '''
                    cd ${WK}/debug
                    cmake .. > /dev/null
                    cmake --build .
                '''            
                break   
            case "any":
                sh '''
                    cd ${WK}/debug
                    cmake .. > /dev/null
                    make
                '''            
                break 
            default :
                sh'''
                cd ${WK}/debug
                cmake .. > /dev/null
                make > /dev/null
                make install > /dev/null
                cd ${WKC}/tests
                pip3 install ${WKC}/src/connector/python/
                '''
        }
    
    }
    
    return 1
}

def abortPreviousBuilds() {
  def currentJobName = env.JOB_NAME
  def currentBuildNumber = env.BUILD_NUMBER.toInteger()
  def jobs = Jenkins.instance.getItemByFullName(currentJobName)
  def builds = jobs.getBuilds()

  for (build in builds) {
    if (!build.isBuilding()) {
      continue;
    }

    if (currentBuildNumber == build.getNumber().toInteger()) {
      continue;
    }

    build.doKill()    //doTerm(),doKill(),doTerm()
  }
}

//  abort previous build
abortPreviousBuilds()
def abort_previous(){
  def buildNumber = env.BUILD_NUMBER as int
  if (buildNumber > 1) milestone(buildNumber - 1)
  milestone(buildNumber)
}