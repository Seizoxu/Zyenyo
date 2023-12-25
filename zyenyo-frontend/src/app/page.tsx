import Image from 'next/image'
import axios from 'axios'
import TestComponent from '@/components/TestComponent'

export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center p-24 bg-[#1E1F22] text-white">
      <div className='flex flex-row gap-0 text-9xl mb-10'>
        <div className='text-white'>Zyenyo</div><div className='text-[#F51A1F]'>Bot</div>
      </div>
      <TestComponent />
    </main>
  )
}
