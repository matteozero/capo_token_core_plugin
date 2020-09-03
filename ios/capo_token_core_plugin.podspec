#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint capo_token_core_plugin.podspec' to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'capo_token_core_plugin'
  s.version          = '0.0.1'
  s.summary          = 'A new flutter plugin project.'
  s.description      = <<-DESC
A new flutter plugin project.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.platform = :ios, '10.0'
  s.swift_versions = ['5.2']

  s.static_framework = true

  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  
  s.dependency 'TokenCoreCapo'
  s.dependency 'CoreBitcoin'
  s.dependency 'OpenSSL-Universal'
  s.dependency 'BigInt'
  s.dependency 'libscrypt'
  s.dependency 'secp256k1'
  s.dependency 'CryptoSwift'




  # Flutter.framework does not contain a i386 slice. Only x86_64 simulators are supported.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES',
#    'VALID_ARCHS[sdk=iphonesimulator*]' => 'x86_64'
  }
end
