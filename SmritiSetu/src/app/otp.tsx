import { View, Text, TouchableOpacity, TextInput, SafeAreaView, KeyboardAvoidingView, Platform } from 'react-native';
import { useRouter } from 'expo-router';
import { useState, useRef } from 'react';
import { ArrowLeft } from 'lucide-react-native';

export default function OTPScreen() {
  const router = useRouter();
  const [code, setCode] = useState(['', '', '', '']);
  const inputs = useRef<Array<TextInput | null>>([]);

  const handleChange = (text: string, index: number) => {
    const newCode = [...code];
    newCode[index] = text;
    setCode(newCode);

    if (text && index < 3) {
      inputs.current[index + 1]?.focus();
    }
  };

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <KeyboardAvoidingView 
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        className="flex-1 justify-center px-6"
      >
        <View className="absolute top-1/4 right-10 w-64 h-64 bg-indigo-500/20 rounded-full blur-3xl opacity-40" />
        
        <View className="bg-white/10 border border-white/20 p-8 rounded-3xl z-10 shadow-xl">
          <TouchableOpacity 
            className="w-10 h-10 bg-white/10 rounded-full items-center justify-center mb-6 border border-white/10"
            onPress={() => router.back()}
          >
            <ArrowLeft color="#fff" size={20} />
          </TouchableOpacity>
          
          <Text className="text-3xl font-bold text-white mb-2">Verification</Text>
          <Text className="text-slate-400 mb-8 font-medium">Enter the 4-digit code sent to your email.</Text>
          
          <View className="flex-row justify-between mb-8 px-2">
            {[0, 1, 2, 3].map((index) => (
              <TextInput 
                key={index}
                ref={(ref) => { inputs.current[index] = ref; }}
                className="bg-slate-900/50 border border-slate-700 text-white text-center rounded-xl text-2xl font-bold w-14 h-16"
                keyboardType="number-pad"
                maxLength={1}
                value={code[index]}
                onChangeText={(text) => handleChange(text, index)}
              />
            ))}
          </View>
          
          <TouchableOpacity 
            className="bg-indigo-500 w-full py-4 rounded-xl items-center shadow-lg shadow-indigo-500/30 mb-6"
            onPress={() => router.push('/reset-password' as any)}
          >
            <Text className="text-white font-bold text-lg">Verify Code</Text>
          </TouchableOpacity>
          
          <View className="flex-row justify-center">
            <Text className="text-slate-400">Didn't receive code? </Text>
            <TouchableOpacity>
              <Text className="text-indigo-400 font-bold">Resend</Text>
            </TouchableOpacity>
          </View>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
