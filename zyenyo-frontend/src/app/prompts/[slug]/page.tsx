"use client"
import Header from "@/components/Navigation/Header";
import { Prompt } from "@/components/PromptsPage/PromptControls";
import axios from "axios";
import { useEffect, useState } from "react";

export default function Prompt({ params }: { params: { slug: string } }) {
    const [prompt, setPrompt] = useState<Prompt | null>(null)

    useEffect(() => {
        async function query() {
            const res = await axios.get(`/api/prompt/${params.slug}`);
            setPrompt(res.data as Prompt)

        }
        query()
    }, [params.slug])


  return (
    <main className="font-sans flex flex-col min-h-screen justify-start bg-zinc-800 text-white">
    <Header />
    <div className="text-3xl font-bold px-8 md:px-24 py-8">{prompt?.title}</div>
    <div className="px-8 md:px-24 pb-8">{prompt?.text}</div>
    <div className="px-8 md:px-24 text-zinc-300">Type Rating: {prompt?.rating.toFixed(2)}</div>
    </main>
  )
}
