"use client"

import axios from "axios"
import {useEffect, useState} from "react"

type BotStats = {
    total_tests: number,
    total_users: number,
    total_prompts: number
}

const BotStats = ({className}: {className: string}) => {
    const [botStats, setBotStats] = useState<BotStats>()
    useEffect(() => {
        async function query() {
            const res = await axios.get("/api/botstats")
            setBotStats(res.data as BotStats)

        }
        query()
    }, [])

    return (
        <div className={className}>
            <div className="border border-zinc-500 my-3 hidden md:block md:w-[600px]" />
            <div className={`flex flex-col items-center gap-6 md:flex-row md:gap-[100px] justify-center`}>
                <div className="flex flex-col gap-2 items-start">
                    <div className="text-[#F51A1F] text-3xl">{botStats?.total_tests}+</div>
                    <div className="text-zinc-300 text-md">Tests Served</div>
                </div>

                <div className="flex flex-col gap-2 items-start">
                    <div className="text-orange-400 text-3xl">{botStats?.total_users}</div>
                    <div className="text-zinc-300 text-md">Active Users</div>
                </div>

                <div className="flex flex-col gap-2 items-start">
                    <div className="text-amber-400 text-3xl">{botStats?.total_prompts}</div>
                    <div className="text-zinc-300 text-md">Typing Prompts</div>
                </div>
            </div>
            <div className="border border-zinc-500 my-3 hidden md:block md:w-[600px]" />
        </div>
    )
}

export default BotStats
