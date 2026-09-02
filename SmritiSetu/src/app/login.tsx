import { View, Text, TouchableOpacity, TextInput, SafeAreaView, KeyboardAvoidingView, Platform } from 'react-native';
import { useRouter } from 'expo-router';
import { useState } from 'react';

export default function LoginScreen() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <KeyboardAvoidingView 
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        className="flex-1 justify-center px-6"
      >
        <View className="absolute top-10 left-10 w-64 h-64 bg-indigo-500/20 rounded-full blur-3xl opacity-40" />
        <View className="absolute bottom-10 right-10 w-64 h-64 bg-purple-500/20 rounded-full blur-3xl opacity-40" />
        
        <View className="bg-white/10 border border-white/20 p-8 rounded-3xl z-10 shadow-xl">
          <Text className="text-3xl font-bold text-white mb-2">Welcome Back</Text>
          <Text className="text-slate-400 mb-8 font-medium">Sign in to continue to SmritiSetu</Text>
          
          <View className="space-y-4 mb-6">
            <View>
              <Text className="text-slate-300 mb-2 font-medium ml-1">Email</Text>
              <TextInput 
                className="bg-slate-900/50 border border-slate-700 text-white px-4 py-4 rounded-xl text-base"
                placeholder="Enter your email"
                placeholderTextColor="#64748b"
                keyboardType="email-address"
                autoCapitalize="none"
                value={email}
                onChangeText={setEmail}
              />
            </View>
            
            <View className="mt-4">
              <Text className="text-slate-300 mb-2 font-medium ml-1">Password</Text>
              <TextInput 
                className="bg-slate-900/50 border border-slate-700 text-white px-4 py-4 rounded-xl text-base"
                placeholder="Enter your password"
                placeholderTextColor="#64748b"
                secureTextEntry
                value={password}
                onChangeText={setPassword}
              />
            </View>
          </View>
          
          <TouchableOpacity 
            className="self-end mb-8"
            onPress={() => router.push('/forgot-password')}
          >
            <Text className="text-indigo-400 font-semibold">Forgot Password?</Text>
          </TouchableOpacity>
          
          <TouchableOpacity 
            className="bg-indigo-500 w-full py-4 rounded-xl items-center shadow-lg shadow-indigo-500/30"
            onPress={() => router.replace('/(tabs)' as any)}
          >
            <Text className="text-white font-bold text-lg">Login</Text>
          </TouchableOpacity>
          
          <View className="flex-row items-center my-6">
            <View className="flex-1 h-[1px] bg-white/10" />
            <Text className="text-slate-400 mx-4 font-medium">Or</Text>
            <View className="flex-1 h-[1px] bg-white/10" />
          </View>
          
          <TouchableOpacity 
            className="bg-white/5 border border-white/10 w-full py-4 rounded-xl items-center mb-8 flex-row justify-center active:bg-white/10"
            onPress={() => router.replace('/(tabs)' as any)}
          >
            <View className="w-5 h-5 bg-white rounded-full items-center justify-center mr-3">
              <Text className="text-slate-950 font-bold text-xs">G</Text>
            </View>
            <Text className="text-white font-bold text-lg">Continue with Google</Text>
          </TouchableOpacity>
          
          <View className="flex-row justify-center">
            <Text className="text-slate-400">Don't have an account? </Text>
            <TouchableOpacity onPress={() => router.push('/signup')}>
              <Text className="text-indigo-400 font-bold">Sign Up</Text>
            </TouchableOpacity>
          </View>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
