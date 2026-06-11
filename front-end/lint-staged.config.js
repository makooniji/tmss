/**
 * Git 提交前对暂存区文件执行格式化，与 prettier.config.js 及现有 lint 脚本保持一致。
 * @see https://github.com/lint-staged/lint-staged
 */
module.exports = {
  '*.{js,jsx,ts,tsx}': ['eslint --fix', 'prettier --write'],
  '*.vue': [
    'eslint --fix',
    'prettier --write',
    'stylelint --fix --allow-empty-input'
  ],
  '*.{css,less,scss}': [
    'prettier --write',
    'stylelint --fix --allow-empty-input'
  ],
  '*.{json,html,md}': ['prettier --write']
}
