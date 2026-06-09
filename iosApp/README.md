# iOS dependency setup

This iOS app uses CocoaPods for `LibSignalClient`.

## Prerequisites

- Use an arm64 Ruby environment instead of the system Ruby gems.
- Homebrew Ruby works on Apple Silicon:
  `PATH=/opt/homebrew/opt/ruby/bin:/opt/homebrew/lib/ruby/gems/4.0.0/bin:$PATH`

## Install pods

From this directory:

```sh
PATH=/opt/homebrew/opt/ruby/bin:/opt/homebrew/lib/ruby/gems/4.0.0/bin:$PATH bundle install
PATH=/opt/homebrew/opt/ruby/bin:/opt/homebrew/lib/ruby/gems/4.0.0/bin:$PATH bundle exec pod install
```

Then open the generated workspace instead of the project file.
