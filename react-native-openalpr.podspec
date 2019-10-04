require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = package['name']
  s.version      = package['version']
  s.summary      = package['description']
  s.license      = package['license']

  s.authors      = package['author']
  s.homepage     = package['homepage']
  s.platform     = :ios, "9.0"

  s.source       = { :git => 'https://github.com/RobertSasak/react-native-openalpr.git', :tag => "v#{s.version}" }
  s.source_files  = 'ios/**/*.{h,m,mm,swift}'

  s.resources = ['ios/Frameworks/openalpr.framework/openalpr.conf', 'ios/Frameworks/openalpr.framework/runtime_data']
  s.frameworks = 'CoreGraphics', 'UIKit'
  s.weak_framework = 'opencv2'
  s.static_framework = true
  s.vendored_frameworks = 'ios/Frameworks/openalpr.framework'
  s.pod_target_xcconfig = { 'ENABLE_BITCODE' => 'NO', 'OTHER_LDFLAGS' => '-lstdc++ -lz -llept -ltesseract_all', 'LIBRARY_SEARCH_PATHS' => '"${PODS_ROOT}/TesseractOCRiOS/TesseractOCR/lib"', 'FRAMEWORK_SEARCH_PATHS' => '"${PODS_ROOT}/OpenCV" "${PODS_ROOT}/TesseractOCRiOS/Products"', 'CLANG_WARN_DOCUMENTATION_COMMENTS' => 'NO' }
  s.dependency 'React'
  s.dependency 'OpenCV', '~> 3.1.0.1'
  s.dependency 'TesseractOCRiOS', '~> 3.03'
end