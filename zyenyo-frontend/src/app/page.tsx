import Image from 'next/image'
import axios from 'axios'
import TestComponent from '@/components/TestComponent'
import Dashboard from '@/components/HomePage/Dashboard'

export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center p-24 bg-zinc-900 text-white">

      <Dashboard/>

      <TestComponent />
    </main>
  )
}
