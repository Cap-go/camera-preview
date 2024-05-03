import antfu from '@antfu/eslint-config'

export default antfu(
  {
    ignores: [
      'build',
      'dist',
      'demo',
      'README.md', // ignore README.md until ionic config work on eslint 9
      'definitions.ts', // ignore README.md until ionic config work on eslint 9
    ],
  },
)
