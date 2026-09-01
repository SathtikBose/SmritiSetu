import { View, Text, TextInput, TouchableOpacity } from 'react-native';
import { Link, useRouter } from 'expo-router';
import { ArrowLeft } from 'lucide-react-native';

export default function OTP() {
  const router = useRouter();

  return (
    <View className="flex-1 px-6 pt-16 bg-slate-900">
      <TouchableOpacity 
        className="mb-8 h-10 w-10 justify-center"
        onPress={() => router.back()}
      >
        <ArrowLeft color="#818cf8" size={24} />
      </TouchableOpacity>

      <Text className="text-3xl font-bold text-indigo-400 mb-2">Enter OTP</Text>
      <Text className="text-slate-400 mb-8">
        We&apos;ve sent a 6-digit code to your email. Please enter it below.
      </Text>
      
      <View className="space-y-4">
        <View className="mb-6">
          <TextInput 
            className="w-full bg-slate-800 text-slate-100 text-center text-2xl tracking-widest px-4 py-4 rounded-lg border border-slate-700"
            placeholder="000000"
            placeholderTextColor="#475569"
            keyboardType="number-pad"
            maxLength={6}
          />
        </View>

        {/* @ts-ignore */}
        <Link href="/login" asChild>
          <TouchableOpacity className="w-full bg-indigo-500 py-3 rounded-lg items-center mt-4">
            <Text className="text-white font-semibold text-lg">Verify & Reset</Text>
          </TouchableOpacity>
        </Link>
        
        <View className="flex-row justify-center mt-6">
          <Text className="text-slate-400">Didn&apos;t receive the code? </Text>
          <TouchableOpacity>
            <Text className="text-indigo-400 font-semibold">Resend</Text>
          </TouchableOpacity>
        </View>
      </View>
    </View>
  );
}
