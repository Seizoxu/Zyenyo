import {useState} from "react"
import {Prompt} from "./PromptControls"
import { ChevronDoubleDownIcon, ChevronDoubleUpIcon } from "@heroicons/react/24/outline";


type PromptCardProps = {
    prompt: Prompt
}

const PromptCard = (props: PromptCardProps) => {
    const [collapsed, setCollapsed] = useState<boolean>(true)
    let { prompt } = props

    return (
        <button className="relative flex p-10 flex-col border-[3px] border-[#F51A1F] rounded-3xl hover:bg-zinc-800" onClick={() => setCollapsed((prev) => !prev)}>
            <div className="flex mb-3 justify-between">
                <p className="text-lg font-bold">{prompt.title}</p>
                <p className="text-md font-bold">TR: {prompt.rating.toFixed(2)}</p>
            </div>
           <p className={`text-sm text-left text-gray-50 ${collapsed ? "truncate" : ""}`}>{prompt.text}</p>
            {collapsed ? (
                <ChevronDoubleDownIcon className="absolute h-6 w-6 left-[50%] right-[50%] bottom-1 text-[#F51A1F]" />
            ) : (
                <ChevronDoubleUpIcon className="absolute h-6 w-6 left-[50%] right-[50%] bottom-1 text-[#F51A1F]" />
            )}
        </button>
    )
}

export default PromptCard
