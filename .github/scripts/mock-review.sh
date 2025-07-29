#!/bin/bash

# 确保脚本在任何命令失败时退出
set -e

echo "🚀 Starting mock code review..."

# =================================================================
# 1. MOCK: 模拟运行 CLI 工具并生成分析结果
# 在这里，我们使用 git diff --stat 来获取真实的文件变更统计
# $BASE_SHA 和 $HEAD_SHA 会由 GitHub Actions workflow 文件传入
# =================================================================
echo "Analysing changes between $BASE_SHA and $HEAD_SHA..."

# 使用 git diff --stat 计算文件变更统计，并将结果存入变量
# 如果 PR 是空的（比如只改了标题），diff 可能会失败，所以用 || true 保证脚本继续
DIFF_STAT=$(git diff --stat $BASE_SHA $HEAD_SHA) || true

# 如果 DIFF_STAT 为空，提供一个默认消息
if [ -z "$DIFF_STAT" ]; then
  DIFF_STAT="没有检测到文件内容变更。"
fi

echo "✅ Analysis complete."

# =================================================================
# 2. 组织最终的评论内容 (Markdown 格式)
# =================================================================
COMMENT_BODY="### 🤖 自动代码审查报告 (Mock)

你好！这是一个自动流程测试。

#### 📊 文件变更统计
以下是本次 PR 中文件变更的统计信息：
\`\`\`diff
${DIFF_STAT}
\`\`\`

#### 📝 Mock 分析结果
- [x] **代码风格**: 检查通过
- [x] **潜在 Bug**: 未发现明显问题
- [ ] **性能**: 有一个待优化的点 (模拟)

**结论**: 流程测试成功！当替换为真实脚本后，这里将展示实际的审查结果。
"

# =================================================================
# 3. 将评论内容输出给 GitHub Actions
# 使用特殊的语法将我们的评论内容传递给 GitHub Actions 的输出变量
# 变量名为 review_comment
# =================================================================
echo "review_comment<<EOF" >> $GITHUB_OUTPUT
echo "$COMMENT_BODY" >> $GITHUB_OUTPUT
echo "EOF" >> $GITHUB_OUTPUT

echo "🏁 Mock review script finished."
