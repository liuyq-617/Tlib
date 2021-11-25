package org.devops

def pre_test(){
    sh'hostname'
    sh '''
    sudo rmtaos || echo "taosd has not installed"
    '''
    sh '''
    killall -9 taosd ||echo "no taosd running"
    killall -9 gdb || echo "no gdb running"
    killall -9 python3.8 || echo "no python program running"
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
    cd debug
    cmake .. > /dev/null
    make > /dev/null
    make install > /dev/null
    cd ${WKC}/tests
    pip3 install ${WKC}/src/connector/python/
    '''
    return 1
}