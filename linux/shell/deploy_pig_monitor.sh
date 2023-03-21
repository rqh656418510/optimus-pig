#!/bin/bash

########################################################
#####       在本地开发环境，一键部署到 Linux 生产环境
#####             必须在 shell 目录下执行脚本
########################################################

# 更新流程
# 1. 在本地执行一键部署脚本： bash deploy_pig_monitor.sh
# 2. 等待 SpringBoot 服务启动完成

# 读取INI配置文件
function __readINI() {
 	INIFILE=$1; SECTION=$2; ITEM=$3
 	_readContent=`awk -F '=' '/\['$SECTION'\]/{a=1}a==1&&$1~/'$ITEM'/{print $2;exit}' $INIFILE`
	echo ${_readContent}
}

# 初始化连接参数
_HOST=( $( __readINI /etc/rsyncd.ini OptimusBaseBackendApp host ) )
_USER=( $( __readINI /etc/rsyncd.ini OptimusBaseBackendApp user ) )
_MODULE=( $( __readINI /etc/rsyncd.ini OptimusBaseBackendApp module ) )
_PASSWORD_FILE=( $( __readINI /etc/rsyncd.ini OptimusBaseBackendApp password_file ) )

# 模块的名称
module_name="pig-monitor"

# 父模块的名称
parent_name="pig-visual"

# 容器名称（Docker）
container_name="optimus-pig-monitor"

# 当前日期
cur_date=$(date "+%Y-%m-%d")
cur_time=$(date "+%H-%M-%S")

# 临时目录的路径
tmp_path="/tmp/build/optimus/$cur_date/$cur_time"

# 进入项目的根目录
cd ../../

# 编译和打包项目
mvn clean install package -Pprod

# 拷贝Jar文件到临时目录
mkdir -p $tmp_path/$module_name
cp -rf ./$parent_name/$module_name/target/$module_name.jar $tmp_path/$module_name/$module_name.jar

# 同步文件到生产服务器（不能使用 "--delete" 参数删除目标目录比源目录多余的文件）
rsync -avzP --no-o --no-g $tmp_path/ $_USER@$_HOST::$_MODULE/ --password-file=$_PASSWORD_FILE

# 等待文件同步完成
sleep 5

# 重启生产服务器上的容器
ssh root@$_HOST "docker-compose -f /usr/local/docker/docker-compose-files/optimus/docker-compose.yml restart $container_name"

# 清理临时目录
rm -rf $tmp_path
