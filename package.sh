#!/bin/zsh

# ===== 配置参数（本机变量） =====
HOST="192.168.31.205"      # 远程主机IP
USERNAME="koji"            # 远程用户名
PASSWORD="nimabi"          # 远程密码
PORT=22                    # SSH端口（默认22）

REMOTE_DIR="/Users/koji/work/sinosoft/apple-batch"  # 远程代码目录
REMOTE_TARGET_DIR="$REMOTE_DIR/target"             # 远程生成的dmg目录
LOCAL_DOWNLOAD_DIR="$HOME/Downloads"                 # 本地保存目录

# ===== 第一步：拉取代码并打包（远程执行） =====
echo "==== 开始远程操作 ===="
sshpass -p "$PASSWORD" ssh -o StrictHostKeyChecking=no -p $PORT $USERNAME@$HOST zsh << 'EOF'
    # 进入代码目录
    cd "/Users/koji/work/sinosoft/apple-batch" || { 
        echo "错误：无法进入目录 /Users/koji/work/sinosoft/apple-batch"; 
        pwd
        exit 1; 
    }
    echo "成功进入目录: $(pwd)"

    # 拉取最新代码
    echo "正在拉取最新代码..."
    export JAVA_HOME=/Users/koji/Library/Java/JavaVirtualMachines/azul-17.0.14/Contents/Home
    export PATH=$JAVA_HOME/bin:$PATH
    git pull || { echo "错误：git pull 失败"; exit 1; }

    # 执行Maven打包
    echo "正在执行Maven打包..."

    /usr/local/bin/mvn clean package || { 
        echo "错误：Maven打包失败"; 
        exit 1; 
    }
EOF

# 检查打包是否成功
if [ $? -ne 0 ]; then
    echo "错误：远程打包失败，终止流程！"
    exit 1
fi

# ===== 第二步：查找最新dmg文件（远程执行） =====
echo "==== 查找最新dmg文件 ===="
LATEST_DMG=$(sshpass -p "$PASSWORD" ssh -o StrictHostKeyChecking=no -p $PORT $USERNAME@$HOST zsh << EOF_FIND
    # 查找target目录下最新的.dmg文件
    LATEST=\$(ls -t "$REMOTE_TARGET_DIR"/*.dmg 2>/dev/null | head -n 1) # Use \$() and quote the path
    if [ -z "\$LATEST" ]; then
        echo "错误：未找到.dmg文件！" >&2 # Echo error to stderr
        exit 1
    fi
    echo "\$LATEST" # Echo the result to stdout
EOF_FIND
)

# 检查是否找到文件
if [ $? -ne 0 ]; then
    echo "错误：查找dmg文件失败！"
    exit 1
fi

# ===== 第三步：下载文件到本地 =====
echo "==== 下载文件到本地 ===="
sshpass -p "$PASSWORD" scp -o StrictHostKeyChecking=no -P $PORT $USERNAME@$HOST:"$LATEST_DMG" "$LOCAL_DOWNLOAD_DIR/"

# 检查下载是否成功
if [ $? -eq 0 ]; then
    echo "下载完成！文件已保存到: $LOCAL_DOWNLOAD_DIR"
    echo "文件名: $(basename "$LATEST_DMG")"
else
    echo "错误：文件下载失败！"
    exit 1
fi

