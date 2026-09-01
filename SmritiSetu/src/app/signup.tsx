import { View, Text, TextInput, TouchableOpacity } from 'react-native';
import { Link } from 'expo-router';

export default function SignUp() {
  return (
    <View className="flex-1 justify-center px-6 bg-slate-900">
      <Text className="text-3xl font-bold text-indigo-400 mb-8 text-center">Create Account</Text>
      
      <View className="space-y-4">
        <View>
          <Text className="text-slate-300 mb-2">Full Name</Text>
          <TextInput 
            className="w-full bg-slate-800 text-slate-100 px-4 py-3 rounded-lg border border-slate-700"
            placeholder="Enter your full name"
            placeholderTextColor="#64748b"
          />
        </View>

        <View>
          <Text className="text-slate-300 mb-2">Email</Text>
          <TextInput 
            className="w-full bg-slate-800 text-slate-100 px-4 py-3 rounded-lg border border-slate-700"
            placeholder="Enter your email"
            placeholderTextColor="#64748b"
            keyboardType="email-address"
            autoCapitalize="none"
          />
        </View>

        <View className="mb-6">
          <Text className="text-slate-300 mb-2">Password</Text>
          <TextInput 
            className="w-full bg-slate-800 text-slate-100 px-4 py-3 rounded-lg border border-slate-700"
            placeholder="Create a password"
            placeholderTextColor="#64748b"
            secureTextEntry
          />
        </View>

        <Link href="/(tabs)" asChild>
          <TouchableOpacity className="w-full bg-indigo-500 py-3 rounded-lg items-center mt-4">
            <Text className="text-white font-semibold text-lg">Sign Up</Text>
          </TouchableOpacity>
        </Link>
        
        <View className="flex-row justify-center mt-6">
          <Text className="text-slate-400">Already have an account? </Text>
          {/* @ts-ignore */}
          <Link href="/login" asChild>
            <TouchableOpacity>
              <Text className="text-indigo-400 font-semibold">Login</Text>
            </TouchableOpacity>
          </Link>
        </View>
      </View>
    </View>
  );
}
