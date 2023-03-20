#!/bin/bash

########################################################
#####       在本地开发环境，一键部署到 Linux 生产环境
#####             必须在 shell 目录下执行脚本
########################################################

# 更新流程
# 1. 在本地执行一键部署脚本： bash deploy_pig_monitor.sh
# 2. 等待 SpringBoot 服务启动完成

# 判断当前用户是否为Root用户
if [ $UID -eq 0 ]; then
  echo -e "\033[31m Error: 请使用普通用户权限执行当前脚本 \033[0m"
  echo -e "\033[31m Error: 程序退出执行 \033[0m"
  exit 1
fi

# 读取INI配置文件
function __readINI() {
 	INIFILE=$1; SECTION=$2; ITEM=$3
 	_readContent=`awk -F '=' '/\['$SECTION'\]/{a=1}a==1&&$1~/'$ITEM'/{print $2;exit}' $INIFILE`
	echo ${_readContent}
}

# 初始化连接参数
_HOST=( $( __readINI /etc/rsyncd.ini OptimusFrontendApp host ) )
_USER=( $( __readINI /etc/rsyncd.ini OptimusFrontendApp user ) )
_MODULE=( $( __readINI /etc/rsyncd.ini OptimusFrontendApp module ) )
_PASSWORD_FILE=( $( __readINI /etc/rsyncd.ini OptimusFrontendApp password_file ) )

# 应用的名称
app_name="pig-monitor"

# 应用的父级目录
parent_name="pig-visual"

# 当前日期
cur_date=$(date "+%Y-%m-%d")
cur_time=$(date "+%H-%M-%S")

# 临时目录的路径
tmp_path="/tmp/build/optimus/$cur_date/$cur_time"

# 进入项目的根目录
cd ../../

# 编译和打包项目
mvn clean && mvn install && mvn package

# 拷贝文件到临时目录
mkdir -p $tmp_path/$app_name
cp -rf ./$parent_name/$app_name/target/$app_name.jar $tmp_path/$app_name/$app_name.jar

# 同步文件到生产服务器（不能使用 "--delete" 参数删除目标目录比源目录多余的文件）
rsync -avzP --no-o --no-g $tmp_path/ $_USER@$_HOST::$_MODULE/ --password-file=$_PASSWORD_FILE

# 等待文件同步完成
sleep 5

# 重启生产服务器上的容器
ssh root@open.techgrow.cn "docker-compose -f /usr/local/docker/docker-compose-files/optimus/docker-compose.yml restart $app_name"

# 清理临时目录
rm -rf $tmp_path
