"use client"

import Link from "next/link"

const Header = () => {
    return (
        <div className=' flex bg-zinc-900 p-6 w-full justify-center gap-0 text-6xl md:text-6xl mb-12'>
        <Link className='flex flex-row' href="/">
            <div className='text-white underline underline-offset-8'>Zyenyo</div><div className='text-[#F51A1F]'>Bot</div>
        </Link>
        </div>
    )
}
export default Header
