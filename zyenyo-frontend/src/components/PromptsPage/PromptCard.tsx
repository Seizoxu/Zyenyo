import {useState} from "react"
import {Prompt} from "./PromptControls"
import { ChevronDoubleDownIcon, ChevronDoubleUpIcon } from "@heroicons/react/24/outline";
import Link from "next/link";


type PromptCardProps = {
    prompt: Prompt
}

const PromptCard = (props: PromptCardProps) => {
    const [collapsed, setCollapsed] = useState<boolean>(true)
    let { prompt } = props

    return (
        <Link href={`/prompts/${prompt.slug}`} className="m-3 flex p-8 flex-col border-[3px] border-[#F51A1F] rounded-3xl hover:bg-zinc-700">
        <p className="text-lg font-bold text-left mb-2">{prompt.title}</p>
            <div className="flex justify-between w-full">
                <div className="text-md">TR: {prompt.rating.toFixed(2)}</div>
                <div className="text-md">Length: {prompt.text.length}</div>
            </div>
        </Link>
    )
}

export default PromptCard
